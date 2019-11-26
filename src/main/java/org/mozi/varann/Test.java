package org.mozi.varann;


import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.g1k.ThousandGenomesAnnotationDriver;
import htsjdk.variant.variantcontext.VariantContext;
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
            logger.info("Loading reference genome");

            dataLoader.init();
            logger.info("\n Saved data");
        } catch (SerializationException | JannovarVarDBException ex) {
            ex.printStackTrace();
        }
    }

    private static void annotateVCF() {
        try {
            VCFFileReader vcfReader = new VCFFileReader(FileUtils.getFile(properties.getProperty("basePath"), "small.vcf"));
            VCFHeader vcfHeader = vcfReader.getFileHeader();
            Stream<VariantContext> stream = vcfReader.iterator().stream();
            DBAnnotationOptions options = DBAnnotationOptions.createDefaults();
            options.setIdentifierPrefix("1K_");
            ThousandGenomesAnnotationDriver thousandGenomeAnno = new ThousandGenomesAnnotationDriver(genomeRepo.findById("1k").get(), refRepo.findById("hg38").get(), options);
            stream = stream.map(thousandGenomeAnno::annotateVariantContext);
            JannovarData data = transcriptRepo.findById("hg38_ensembl").get();
            VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());
            stream = stream.map(annotator::annotateVariantContext);
            stream.forEach(vc -> {
                logger.info("VC: " + vc.toString());
            });
            logger.info("Finished Annotation");
        }
        catch (JannovarVarDBException ex) {
            ex.printStackTrace();
        }
    }
}
