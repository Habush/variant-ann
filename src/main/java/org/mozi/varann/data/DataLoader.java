package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
/**
 * author: Abdulrahman Semrie
 *
 */

@Component
public class DataLoader {
        @Autowired
        private TranscriptDbRepository transcriptRepo;
        @Autowired
        private ReferenceRepository refRepo;

        private File basePath;
        @Getter
        private HashMap<String, String> dbPathMap = new HashMap<>();
        private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

        public DataLoader(@Value("${basePath}") String path) {
            this.basePath = new File(path);
        }

        public void init() throws JannovarVarDBException, SerializationException {
            logger.info("Loading Transcripts");
            loadTranscripts();;
            logger.info("Loading Reference DBs");
            loadReferences();
            loadDbPath();
        }

        private void loadDbPath() {
            File[] vcfiles = basePath.listFiles((dir, name) -> name.endsWith(".vcf.gz"));
            assert vcfiles != null && vcfiles.length > 0;

            for (File file : vcfiles) {
                String name = file.getName();
                if(name.contains("1000GENOME")){
                    dbPathMap.put("1k", file.getAbsolutePath());
                }else if(name.contains("clinvar")){
                    dbPathMap.put("clinvar", file.getAbsolutePath());
                } else if(name.contains("dbSNP")){
                    dbPathMap.put("dbsnp", file.getAbsolutePath());
                } else if(name.contains("cosmic")){
                    dbPathMap.put("cosmic", file.getAbsolutePath());
                }else {
                    throw new IllegalArgumentException("Unknown vcf file exception " + name);
                }
            }

        }

        private void loadTranscripts() throws SerializationException {
            File[] transFiles = basePath.listFiles((dir, name) -> name.endsWith(".ser"));

            assert transFiles != null;
            assert transFiles.length > 0;
            for(File file : transFiles) {
                logger.info("Loading transcript file " + file.getName());
                String name = file.getName().split("\\.")[0];
                transcriptRepo.save(name,  DataReader.readSerializedObj(file.getPath()));
            }
        }

        private void loadReferences() throws JannovarVarDBException{
            File[] fastFiles = basePath.listFiles((dir, name) -> name.endsWith(".fa"));

            assert fastFiles != null;
            for(File file : fastFiles) {
                logger.info("Loading fasta file " + file.getName());
                String name = null;
                if (file.getName().matches("(.*)(h38)(.*)*")){
                    name = "hg38";
                } else if(file.getName().matches("(.*)(h19)(.*)*")) {
                    name = "hg19";
                } else if(file.getName().matches("(.*)(h37)(.*)*")) {
                    name = "hg37";
                }
                assert name != null;

                refRepo.save(name, new AlleleMatcher(file.getPath()));
            }
        }



}
