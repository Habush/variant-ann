package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.variant.vcf.VCFFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * author: Abdulrahman Semrie
 *
 */
@Component
public class DataLoader {
        @Autowired
        private TranscriptDbRepository transcriptRepo;
        @Autowired
        private GenomeDbRepository genomeRepo;
        @Autowired
        private ReferenceRepository refRepo;

        private File basePath;

        private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

        public DataLoader(String path) {
            this.basePath = new File(path);
        }

        public void init() throws JannovarVarDBException, SerializationException {
            logger.info("Loading Transcripts");
            loadTranscripts();;
            logger.info("Loading Reference DBs");
            loadReferences();
            logger.info("Loading 1000 Genome data");
            load1kGenomicDb();
        }

        private void load1kGenomicDb() {
            File[] vcfiles = basePath.listFiles((dir, name) -> name.startsWith("1000GENOMES") && name.endsWith(".vcf.gz"));
            assert vcfiles != null;
            VCFFileReader vcfReader = DataReader.readVCF(vcfiles[0].getPath());
            genomeRepo.save("1k", vcfReader);
        }

        private void loadTranscripts() throws SerializationException {
            File[] transFiles = basePath.listFiles((dir, name) -> name.endsWith(".ser"));

            assert transFiles != null;
            assert transFiles.length > 0;
            for(File file : transFiles) {
                String name = file.getName().split(".")[0];
                transcriptRepo.save(name,  DataReader.readSerializedObj(file.getPath()));
            }
        }

        private void loadReferences() throws JannovarVarDBException{
            File[] fastFiles = basePath.listFiles((dir, name) -> name.endsWith(".fa"));

            assert fastFiles != null;
            for(File file : fastFiles) {
                String name = null;
                if (file.getPath().matches("(.*)(h38)(.*)*")){
                    name = "hg38";
                } else if(file.getPath().matches("(.*)(h19)(.*)*")) {
                    name = "hg19";
                } else if(file.getPath().matches("(.*)(h37)(.*)*")) {
                    name = "hg37";
                }
                assert name != null;

                refRepo.save(name, new AlleleMatcher(file.getPath()));
            }
        }



}
