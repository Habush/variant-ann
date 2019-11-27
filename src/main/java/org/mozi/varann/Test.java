package org.mozi.varann;


import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import de.charite.compbio.jannovar.htsjdk.VariantEffectHeaderExtender;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import de.charite.compbio.jannovar.mendel.filter.ConsumerProcessor;
import de.charite.compbio.jannovar.mendel.filter.VariantContextProcessor;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.facade.DBVariantContextAnnotator;
import de.charite.compbio.jannovar.vardbs.g1k.ThousandGenomesAnnotationDriver;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.mozi.varann.data.DataLoader;
import org.mozi.varann.data.GenomeDbRepository;
import org.mozi.varann.data.ReferenceRepository;
import org.mozi.varann.data.TranscriptDbRepository;
import org.mozi.varann.data.config.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Test {

    private static AnnotationConfigApplicationContext ctx;
    private static Ignite ignite;
    private static IgniteCache<String, AlleleMatcher> refRepo;
    private static IgniteCache<String, JannovarData> transcriptRepo;
    private static IgniteCache<String, VCFFileReader> genomeRepo;
    private static DataLoader dataLoader;
    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        properties  = new Properties();
        try(InputStream in = Test.class.getResourceAsStream("/application.properties")) {
            properties.load(in);
            igniteSpringDataInit();

            dataLoader = new DataLoader(properties.getProperty("basePath"), transcriptRepo, refRepo, genomeRepo);
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(Test::populateRepository, executorService).thenRunAsync(Test::annotateVCF).thenAccept((vc) ->  ctx.destroy());
            executorService.shutdown();
            logger.info("Waiting for other threads to finish....");
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }

    /**
     * Initializes Spring Data and Ignite repositories.
     */
    private static void igniteSpringDataInit() {
        ctx = new AnnotationConfigApplicationContext();

        // Explicitly registering Spring configuration.
        ctx.register(SpringConfig.class);

        ctx.refresh();
        ignite = ctx.getBean(Ignite.class);
        refRepo = ignite.getOrCreateCache("refCache");
        transcriptRepo = ignite.getOrCreateCache("transcriptCache");
        genomeRepo = ignite.getOrCreateCache("genomeCache");
    }

    private static Void populateRepository() {

        try {
            dataLoader.init();
            logger.info("\n Saved data");
        } catch (SerializationException | JannovarVarDBException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static VariantContextWriter buildVariantContextWrite(VCFHeader header, String filename) {
        VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
        builder.setOutputFile(new File(filename));
        builder.setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER);
        builder.setOutputFileType(VariantContextWriterBuilder.OutputType.VCF);

        if (header.getSequenceDictionary() == null) {
            builder.unsetOption(Options.INDEX_ON_THE_FLY);
        }else {
            builder.unsetOption(Options.INDEX_ON_THE_FLY);
        }

        VariantContextWriter out = builder.build();
        VCFHeader updatedHeader = VariantContextWriterConstructionHelper.extendHeaderFields(new VCFHeader(header));

        out.writeHeader(updatedHeader);

        return out;
    }


    private static Void annotateVCF() {
        try(VCFFileReader vcfReader = new VCFFileReader(FileUtils.getFile(properties.getProperty("basePath"), "example.vcf"), false)) {
            String basePath = properties.getProperty("basePath");
            logger.info("Starting annotation...");

            IgniteCache<String, BinaryObject> transCache = transcriptRepo.withKeepBinary();
            IgniteCache<String, BinaryObject> refCache = refRepo.withKeepBinary();
            IgniteCache<String, BinaryObject> genomeCache = genomeRepo.withKeepBinary();

            VCFHeader vcfHeader = vcfReader.getFileHeader();
            Stream<VariantContext> stream = vcfReader.iterator().stream();
            DBAnnotationOptions options = DBAnnotationOptions.createDefaults();
            options.setIdentifierPrefix("1K_");
            VCFFileReader vcfFileReader = genomeCache.get("genomeCache").deserialize();
            DBVariantContextAnnotator thousandGenomeAnno = new DBVariantContextAnnotator(new ThousandGenomesAnnotationDriver(vcfFileReader, refCache.get("hg38").deserialize(), options), options);
            thousandGenomeAnno.extendHeader(vcfHeader);
            stream = stream.map(thousandGenomeAnno::annotateVariantContext);
            JannovarData data = transCache.get("hg38_ensembl").deserialize();
            assert data != null;
            VariantEffectHeaderExtender effectHeader = new VariantEffectHeaderExtender();
            effectHeader.addHeaders(vcfHeader);
            VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());
            stream = stream.map(annotator::annotateVariantContext);

            try(VariantContextWriter writer = buildVariantContextWrite(vcfHeader, PathUtil.join(basePath, "output.vcf"));
                VariantContextProcessor processor = new ConsumerProcessor(vc -> writer.add(vc))
            ){
               stream.forEachOrdered(processor::put);
            }
            logger.info("Wrote annotation result to output.vcf");
            logger.info("Finished Annotation");
        }
        catch (JannovarVarDBException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
