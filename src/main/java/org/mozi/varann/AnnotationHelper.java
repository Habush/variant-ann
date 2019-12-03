package org.mozi.varann;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import de.charite.compbio.jannovar.htsjdk.VariantEffectHeaderExtender;
import de.charite.compbio.jannovar.mendel.filter.ConsumerProcessor;
import de.charite.compbio.jannovar.mendel.filter.VariantContextProcessor;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationDriver;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.DatabaseVariantContextProvider;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.facade.DBVariantContextAnnotator;
import de.charite.compbio.jannovar.vardbs.facade.DBVariantContextAnnotatorFactory;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mozi.varann.data.DataLoader;
import org.mozi.varann.data.ReferenceRepository;
import org.mozi.varann.data.TranscriptDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AnnotationHelper {
    @Autowired
    private DataLoader dataLoader;
    @Autowired
    private TranscriptDbRepository transcriptRepo;
    @Autowired
    private ReferenceRepository referenceRepo;

    private static final Logger logger = LogManager.getLogger(AnnotationHelper.class);

    public AnnotationHelper() {
    }

    public VariantContext parseGenomeChange(String changeStr, ReferenceDictionary refDict) {
        //chr2:109090A>C
        Pattern pat = Pattern.compile("(chr[0-9MXY]+):([0-9]+)([ACGTN]*)>([ACGTN]*)");
        Matcher mat = pat.matcher(changeStr);
        if (!mat.matches()) {
            logger.error("Input string for the chromosomal change " + changeStr
                    + "is invalid. Example: chr21:866511A>C");
        }

        String chr = refDict.getContigIDToName().get(refDict.getContigNameToID().get(mat.group(1)));
        int pos = Integer.parseInt(mat.group(2));
        Allele refA = Allele.create(mat.group(3), true);
        Allele allele = Allele.create(mat.group(4));

        VariantContextBuilder builder = new VariantContextBuilder("", chr, pos, pos, Arrays.asList(refA, allele));

        return builder.make();
    }

    public List<DBVariantContextAnnotator> getAnnotationDriver(String[] dbs, String ref) throws JannovarVarDBException {
        List<DBVariantContextAnnotator> drivers = new ArrayList<>();
        for (String db : dbs) {
            switch (db) {
                case "1k":
                    drivers.add(new DBVariantContextAnnotatorFactory().constructThousandGenomes(dataLoader.getDbPathMap().get("1k"), referenceRepo.findById(ref).get(), new DBAnnotationOptions(true, false, "1K_", DBAnnotationOptions.MultipleMatchBehaviour.BEST_ONLY)));
                    break;
                case "clinvar":
                    drivers.add(new DBVariantContextAnnotatorFactory().constructClinVar(dataLoader.getDbPathMap().get("clinvar"), referenceRepo.findById(ref).get(), new DBAnnotationOptions(true, false, "Clinvar_", DBAnnotationOptions.MultipleMatchBehaviour.BEST_ONLY)));
                    break;
                case "dbsnp":
                    drivers.add(new DBVariantContextAnnotatorFactory().constructDBSNP(dataLoader.getDbPathMap().get("dbsnp"), referenceRepo.findById(ref).get(), new DBAnnotationOptions(true, false, "DbSNP_", DBAnnotationOptions.MultipleMatchBehaviour.BEST_ONLY)));
                    break;
                case "cosmic":
                    drivers.add(new DBVariantContextAnnotatorFactory().constructCosmic(dataLoader.getDbPathMap().get("cosmic"), referenceRepo.findById(ref).get(), new DBAnnotationOptions(true, false, "Cosmic_", DBAnnotationOptions.MultipleMatchBehaviour.BEST_ONLY)));
                    break;
                case "exac":
                    drivers.add(new DBVariantContextAnnotatorFactory().constructExac(dataLoader.getDbPathMap().get("exac"), referenceRepo.findById(ref).get(), new DBAnnotationOptions(true, false, "Exac_", DBAnnotationOptions.MultipleMatchBehaviour.BEST_ONLY)));
                    break;
                default:
                    throw new IllegalArgumentException(db + " Annotation DB has no be implemented yet :(");
            }
        }

        return drivers;

    }

    public VariantContext annotateVariant(String changeStr, String[] dbs, String ref, String transcript) {
        try {
            JannovarData data = transcriptRepo.findById(transcript).get();
            VariantContext vc = parseGenomeChange(changeStr, data.getRefDict());
            List<DBVariantContextAnnotator> drivers = getAnnotationDriver(dbs, ref);

            VariantContext resultVc = null;
            for (DBVariantContextAnnotator driver: drivers) {
                resultVc = driver.annotateVariantContext(vc);
            }

            VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());
            resultVc = annotator.annotateVariantContext(resultVc);

            return resultVc;
        } catch (JannovarVarDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VariantContext annotateVariantById(String id, String[] dbs, String ref, String transcript) {
        try {
            JannovarData data = transcriptRepo.findById(transcript).get();
            List<DBVariantContextAnnotator> drivers = getAnnotationDriver(dbs, ref);

            VariantContext resultVc = null;
            for (DBVariantContextAnnotator driver: drivers) {
                resultVc = driver.annotateVariantContext(id);
            }

            VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());
            resultVc = annotator.annotateVariantContext(resultVc);

            return resultVc;
        } catch (JannovarVarDBException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * This method annotates a vcf using the database specified
     *
     * @param file       - the uploaded vcf file
     * @param dbs         - the databases selected for annotation
     * @param ref        - the reference database
     * @param transcript - the transcript database
     * @return - a vcf {@link File} that contains the annotated result
     */
    public String annotateVcf(MultipartFile file, String[] dbs, String ref, String transcript) {

        try {
            File vcfFile = File.createTempFile("request", "vcf");

            vcfFile.deleteOnExit();
            file.transferTo(vcfFile);
            try (VCFFileReader vcfFileReader = new VCFFileReader(vcfFile, false)) {
                VCFHeader header = vcfFileReader.getFileHeader();
                Stream<VariantContext> stream = vcfFileReader.iterator().stream();
                List<DBVariantContextAnnotator> drivers = getAnnotationDriver(dbs, ref);

                for(DBVariantContextAnnotator driver : drivers){
                    driver.extendHeader(header);
                    stream = stream.map(driver::annotateVariantContext);
                }

                JannovarData data = transcriptRepo.findById(transcript).get();
                VariantEffectHeaderExtender extender = new VariantEffectHeaderExtender();
                extender.addHeaders(header);
                VariantContextAnnotator annotator = new VariantContextAnnotator(data.getRefDict(), data.getChromosomes());

                stream = stream.map(annotator::annotateVariantContext);

                File resultFile = File.createTempFile(dbs[0] + "-" + ref, ".vcf");
                resultFile.deleteOnExit();
                try (VariantContextWriter writer = VariantContextWriterConstructionHelper.openVariantContextWriter(header, resultFile.getPath());
                     VariantContextProcessor sink = new ConsumerProcessor(writer::add)) {
                    stream.forEachOrdered(sink::put);
                }

                return resultFile.getAbsolutePath();

            }
        } catch (IOException | JannovarVarDBException e) {
            e.printStackTrace();
        }
        return null;
    }


}
