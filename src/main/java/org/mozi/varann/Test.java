package org.mozi.varann;


import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import de.charite.compbio.jannovar.htsjdk.VariantEffectHeaderExtender;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import de.charite.compbio.jannovar.mendel.filter.ConsumerProcessor;
import de.charite.compbio.jannovar.mendel.filter.VariantContextProcessor;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.facade.DBVariantContextAnnotator;
import de.charite.compbio.jannovar.vardbs.g1k.ThousandGenomesAnnotationDriver;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.commons.io.FileUtils;
import org.mozi.varann.data.DataLoader;
import org.mozi.varann.data.GenomeDbRepository;
import org.mozi.varann.data.ReferenceRepository;
import org.mozi.varann.data.TranscriptDbRepository;
import org.mozi.varann.data.config.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Stream;

public class Test {

    private static AnnotationConfigApplicationContext ctx;
    private static ReferenceRepository refRepo;
    private static TranscriptDbRepository transcriptRepo;
    private static GenomeDbRepository genomeRepo;
    private static DataLoader dataLoader;
    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        properties  = new Properties();
        try(InputStream in = Test.class.getResourceAsStream("/application.properties")) {
            properties.load(in);
            igniteSpringDataInit();
            dataLoader = new DataLoader(properties.getProperty("basePath"), transcriptRepo, genomeRepo, refRepo);
            populateRepository();
            annotateVCF();
            ctx.destroy();
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
        refRepo = ctx.getBean(ReferenceRepository.class);
        transcriptRepo = ctx.getBean(TranscriptDbRepository.class);
        genomeRepo = ctx.getBean(GenomeDbRepository.class);
    }

    private static void populateRepository() {

        try {
            dataLoader.init();
            logger.info("\n Saved data");
        } catch (SerializationException | JannovarVarDBException ex) {
            ex.printStackTrace();
        }
    }

    private static void annotateVCF() {
        try {
            logger.info("Starting annotation...");
            VCFFileReader vcfReader = new VCFFileReader(FileUtils.getFile(properties.getProperty("basePath"), "example.vcf"), false);
            VCFHeader vcfHeader = vcfReader.getFileHeader();
            Stream<VariantContext> stream = vcfReader.iterator().stream();
            DBAnnotationOptions options = DBAnnotationOptions.createDefaults();
            options.setIdentifierPrefix("1K_");
            DBVariantContextAnnotator thousandGenomeAnno = new DBVariantContextAnnotator(new ThousandGenomesAnnotationDriver(genomeRepo.findById("1k").get(), refRepo.findById("hg38").get(), options), options);
            thousandGenomeAnno.extendHeader(vcfHeader);
            stream = stream.map(thousandGenomeAnno::annotateVariantContext);
            JannovarData data = new JannovarDataSerializer(PathUtil.join(properties.getProperty("basePath"), "hg38_ensembl.ser")).load();
            assert data != null;
            logger.info("Sanity check. There are " + data.getChromosomes().size() + " chromosomes");
            VariantEffectHeaderExtender effectHeader = new VariantEffectHeaderExtender();
            effectHeader.addHeaders(vcfHeader);
            VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());
            stream = stream.map(annotator::annotateVariantContext);

            try(VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(vcfHeader, PathUtil.join(properties.getProperty("basePath"), "output.vcf"));
                VariantContextProcessor processor = new ConsumerProcessor(vc -> writer.add(vc))
            ){
                stream.forEachOrdered(processor::put);
            }
            logger.info("Wrote annotation result to output.vcf");
            logger.info("Finished Annotation");
        }
        catch (JannovarVarDBException | SerializationException ex) {
            ex.printStackTrace();
        }
    }
}
