package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.AsciiLineReaderIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import lombok.Getter;
import org.apache.commons.net.imap.IMAP;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mozi.varann.data.fs.FileSystemWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

/**
 * author: Abdulrahman Semrie
 */

@Component
public class DataLoader {
    @Autowired
    private TranscriptDbRepository transcriptRepo;
    @Autowired
    private ReferenceRepository refRepo;
    @Autowired
    private FileSystemWrapper fileSystemWrapper;
    @Autowired
    private Ignite ignite;

    private File basePath;
    @Getter
    private HashMap<String, String> dbPathMap = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(DataLoader.class);

    public DataLoader(@Value("${basePath}") String path) {
        this.basePath = new File(path);
    }

    public void init() throws JannovarVarDBException, SerializationException {
        logger.info("Loading Transcripts");
        loadTranscripts();
        ;
        logger.info("Loading Reference DBs");
        loadReferences();
        loadDbPath();
        loadGenomeCache();
    }

    private void loadDbPath() {
        logger.info("Getting the annotation db paths");
        File[] vcfiles = basePath.listFiles((dir, name) -> name.endsWith(".vcf.gz"));
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
                throw new IllegalArgumentException("Unknown vcf file exception " + name);
            }
        }

    }

    private void loadTranscripts() throws SerializationException {
        File[] transFiles = basePath.listFiles((dir, name) -> name.endsWith(".ser"));

        assert transFiles != null;
        assert transFiles.length > 0;
        for (File file : transFiles) {
            logger.info("Loading transcript file " + file.getName());
            String name = file.getName().split("\\.")[0];
            transcriptRepo.save(name, DataReader.readSerializedObj(file.getPath()));
        }
    }

    private void loadReferences() throws JannovarVarDBException {
        File[] fastFiles = basePath.listFiles((dir, name) -> name.endsWith(".fa"));

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
    }

    /**
     * This method patch loads {@link htsjdk.variant.variantcontext.VariantContext}s from vcf files and puts them in to an
     * {@link org.apache.ignite.IgniteCache}
     */
    private void loadGenomeCache() {
        logger.info("Loading VCF files into cache");
        try (IgniteDataStreamer<String, List<VariantContext>> dataStreamer = ignite.dataStreamer("genomeCache")) {
            this.dbPathMap.entrySet().parallelStream().forEach(entry -> {
                try {
                    String path = entry.getValue();
                    final VCFCodec vcfCodec = getVCFCodec(path);
                    try (Stream<String> lines = Files.lines(Paths.get(path))) {
                        //filter out header files
                        logger.info("Loading " + entry.getKey() + "....");
                        dataStreamer.addData(entry.getKey(), lines.filter(line -> !line.startsWith("#"))
                                .map(vcfCodec::decode).collect(Collectors.toList()));

                    }
                } catch (IOException ex) {
                    logger.warn("Encountered IOException while reading " + entry.getKey());
                    ex.printStackTrace();
                }

            });
        }
    }

    private VCFCodec getVCFCodec(String path) throws IOException {
        try (SeekableStream headerIn =
                     fileSystemWrapper.open(getFirstPath(path))) {
            InputStream is = bufferAndDecompressIfNecessary(headerIn);
            VCFCodec vcfCodec = new VCFCodec();
            vcfCodec.readHeader(new AsciiLineReaderIterator(AsciiLineReader.from(is)));
            return vcfCodec;
        }
    }

    private String getFirstPath(String path) throws IOException {
        String firstPath;
        if (fileSystemWrapper.isDirectory(path)) {
            firstPath = fileSystemWrapper.firstFileInDirectory(path);
        } else {
            firstPath = path;
        }
        return firstPath;
    }

    private static InputStream bufferAndDecompressIfNecessary(final InputStream in)
            throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        // despite the name, SamStreams.isGzippedSAMFile looks for any gzipped stream (including block
        // compressed)
        return IOUtil.isGZIPInputStream(bis) ? new GZIPInputStream(bis) : bis;
    }


}
