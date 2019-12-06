package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * author: Abdulrahman Semrie
 */

@Service
@RequiredArgsConstructor
public class DataLoader {

    @Value("${basePath}")
    private String basePath;
   /* private final Ignite ignite;
    private final TranscriptDbRepository transcriptRepo;
    private final ReferenceRepository refRepo;*/

    @Getter
    private HashMap<String, String> dbPathMap = new HashMap<>();
    private static Logger logger = LogManager.getLogger(DataLoader.class);

    public void init() throws JannovarVarDBException, SerializationException {
        loadDbPath();
        loadGenomeCache();
        /*logger.info("Loading Transcripts");
        loadTranscripts();
        logger.info("Loading Reference DBs");
        loadReferences();*/
    }

    public void loadDbPath() {
        logger.info("Getting the annotation db paths");
        File[] vcfiles = new File(basePath).listFiles((dir, name) -> name.endsWith(".vcf.gz"));
        assert vcfiles != null && vcfiles.length > 0;

        for (File file : vcfiles) {
            String name = file.getName();
            if (name.contains("1000GENOME")) {
                dbPathMap.put("1k", file.getAbsolutePath());
            } else if (name.contains("clinvar")) {
                dbPathMap.put("clinvar", file.getAbsolutePath());
            } else if (name.contains("dbSNP")) {
                dbPathMap.put("dbsnp", file.getAbsolutePath());
            } else if (name.contains("cosmic")) {
                dbPathMap.put("cosmic", file.getAbsolutePath());
            } else if (name.contains("exac")) {
                dbPathMap.put("exac", file.getAbsolutePath());
            } else {
                logger.info("Putting in " + name + " in the table");
                dbPathMap.put(name.split("\\.")[0], file.getAbsolutePath());
            }
        }

    }

   /* public void loadTranscripts() throws SerializationException {
        File[] transFiles = new File(basePath).listFiles((dir, name) -> name.endsWith(".ser"));

        assert transFiles != null;
        assert transFiles.length > 0;
        for (File file : transFiles) {
            logger.info("Loading transcript file " + file.getName());
            String name = file.getName().split("\\.")[0];
            transcriptRepo.save(name, DataReader.readSerializedObj(file.getPath()));
        }
    }

    public void loadReferences() throws JannovarVarDBException {
        File[] fastFiles = new File(basePath).listFiles((dir, name) -> name.endsWith(".fa"));

        assert fastFiles != null;
        for (File file : fastFiles) {
            logger.info("Loading fasta file " + file.getName());
            String name = null;
            if (file.getName().matches("(.*)(h38)(.*)*")) {
                name = "hg38";
            } else if (file.getName().matches("(.*)(h19)(.*)*")) {
                name = "hg19";
            } else if (file.getName().matches("(.*)(h37)(.*)*")) {
                name = "hg37";
            }
            assert name != null;

            refRepo.save(name, new AlleleMatcher(file.getPath()));
        }
    }*/

    /**
     * This method patch loads {@link htsjdk.variant.variantcontext.VariantContext}s from vcf files and puts them in to an
     * {@link org.apache.ignite.IgniteCache}
     */
    public void loadGenomeCache() {
        /*logger.info("Loading VCF files into cache");
        if(!ignite.cacheNames().contains("genomeCache")){
            ignite.createCache("genomeCache");
        }
        try (IgniteDataStreamer<String, List<VariantContext>> dataStreamer = ignite.dataStreamer("genomeCache")) {
            dataStreamer.autoFlushFrequency(100);
            this.dbPathMap.entrySet().parallelStream().forEach(entry -> {
                try {
                    String path = entry.getValue();
                    VCFCodec vcfCodec = getVCFCodec(path);
                    vcfCodec.setVCFHeader(vcfCodec.getHeader(), vcfCodec.getVersion());
                    try (Reader decoder = new InputStreamReader(bufferAndDecompressIfNecessary(new FileInputStream(path)), StandardCharsets.ISO_8859_1);
                         BufferedReader bufReader = new BufferedReader(decoder);
                         Stream<String> lines = bufReader.lines()) {
                        //filter out header files
                        logger.info("Loading " + entry.getKey() + "....");
                        List<VariantContext> vcs = lines.filter(line -> !line.startsWith("#"))
                                .map(vcfCodec::decode).collect(Collectors.toList());
                        dataStreamer.addData(entry.getKey(), vcs);

                    }
                } catch (IOException ex) {
                    logger.warn("Encountered IOException while reading " + entry.getKey());
                    ex.printStackTrace();
                }

            });

            logger.info("Done loading into genome cache");
        }*/
        this.dbPathMap.entrySet().parallelStream().forEach(entry -> {
            try {
                String path = entry.getValue();
                VCFCodec vcfCodec = getVCFCodec(path);
                vcfCodec.setVCFHeader(vcfCodec.getHeader(), vcfCodec.getVersion());
                try (Reader decoder = new InputStreamReader(bufferAndDecompressIfNecessary(new FileInputStream(path)), StandardCharsets.ISO_8859_1);
                     BufferedReader bufReader = new BufferedReader(decoder);
                     Stream<String> lines = bufReader.lines()) {
                    //filter out header files
                    logger.info("Loading " + entry.getKey() + "....");
                    List<VariantContext> vcs = lines.filter(line -> !line.startsWith("#"))
                            .map(vcfCodec::decode).collect(Collectors.toList());
                    logger.info("Finished loading " + vcs.size() + " variants!!");
                }
            } catch (IOException ex) {
                logger.warn("Encountered IOException while reading " + entry.getKey());
                ex.printStackTrace();
            }

        });
    }

    private VCFCodec getVCFCodec(String path) throws IOException {
        try (InputStream is = bufferAndDecompressIfNecessary(new FileInputStream(path));) {

            VCFCodec vcfCodec = new VCFCodec();
            FeatureCodecHeader featureCodecHeader =  vcfCodec.readHeader(new LineIteratorImpl(AsciiLineReader.from(is)));
            vcfCodec.setVCFHeader((VCFHeader)featureCodecHeader.getHeaderValue(), vcfCodec.getVersion());
            return vcfCodec;
        }
    }

    private static InputStream bufferAndDecompressIfNecessary(final InputStream in)
            throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        // despite the name, SamStreams.isGzippedSAMFile looks for any gzipped stream (including block
        // compressed)
        return IOUtil.isGZIPInputStream(bis) ? new GZIPInputStream(bis) : bis;
    }


}
