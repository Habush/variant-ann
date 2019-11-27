package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * author: Abdulrahman Semrie
 *
 */
public class DataLoader {
        private IgniteCache<String, JannovarData> transcriptRepo;
        private IgniteCache<String, VCFFileReader> genomeRepo;
        private IgniteCache<String, AlleleMatcher> refRepo;

        private File basePath;

        private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

        public DataLoader(String path, IgniteCache<String, JannovarData> transcriptRepo, IgniteCache<String, AlleleMatcher> refRepo, IgniteCache<String, VCFFileReader> genomeRepo) {
            this.basePath = new File(path);
            this.transcriptRepo = transcriptRepo;
            this.refRepo = refRepo;
            this.genomeRepo = genomeRepo;
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
            File[] indexFiles = basePath.listFiles((dir, name) -> name.startsWith("1000GENOMES") && name.endsWith(".tbi"));
            assert vcfiles != null && indexFiles != null;
            VCFFileReader vcfReader = new VCFFileReader(new File(vcfiles[0].getPath()), new File(indexFiles[0].getPath()), true);
            genomeRepo.put("1k", vcfReader);
        }

        private void loadTranscripts() throws SerializationException {
            File[] transFiles = basePath.listFiles((dir, name) -> name.endsWith(".ser"));

            assert transFiles != null;
            assert transFiles.length > 0;
            for(File file : transFiles) {
                logger.info("Loading transcript file " + file.getName());
                String name = file.getName().split("\\.")[0];
                transcriptRepo.put(name,  DataReader.readSerializedObj(file.getPath()));
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

                refRepo.put(name, new AlleleMatcher(file.getPath()));
            }
        }



}
