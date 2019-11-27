package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * author: Abdulrahman Semrie
 *
 */
public class DataLoader {
        private TranscriptDbRepository transcriptRepo;
        private GenomeDbRepository genomeRepo;
        private ReferenceRepository refRepo;

        private File basePath;

        private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

        public DataLoader(String path, TranscriptDbRepository transRepo, GenomeDbRepository gRepo, ReferenceRepository refRepo) {
            this.transcriptRepo = transRepo;
            this.genomeRepo = gRepo;
            this.refRepo = refRepo;
            this.basePath = new File(path);
        }

        public void init() throws JannovarVarDBException, SerializationException {
            logger.info("Loading Transcripts");
            loadTranscripts();;
            logger.info("Loading Reference DBs");
            loadReferences();
//            logger.info("Loading 1000 Genome data");
//            load1kGenomicDb();
        }

        private void load1kGenomicDb() {
            File[] vcfiles = basePath.listFiles((dir, name) -> name.startsWith("1000GENOMES") && name.endsWith(".vcf.gz"));
            File[] indexFiles = basePath.listFiles((dir, name) -> name.startsWith("1000GENOMES") && name.endsWith(".tbi"));
            assert vcfiles != null && indexFiles != null;
            VCFFileReader vcfReader = new VCFFileReader(new File(vcfiles[0].getPath()), new File(indexFiles[0].getPath()), true);
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
