package org.mozi.varann.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.mozi.varann.data.impl.ACMGRecordConverter;
import org.mozi.varann.data.impl.gnomad.GnomadExomeRecordConverter;
import org.mozi.varann.data.impl.annotation.VariantContextToEffectRecordConverter;
import org.mozi.varann.data.impl.clinvar.ClinVarVariantContextToRecordConverter;
import org.mozi.varann.data.impl.dbnsfp.DBNSFPRecordConverter;
import org.mozi.varann.data.impl.exac.ExacVariantContextToRecordConverter;
import org.mozi.varann.data.impl.g1k.ThousandGenomesVariantContextToRecordConverter;
import org.mozi.varann.data.impl.genes.GeneRecordConverter;
import org.mozi.varann.data.records.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 * Loads the genome data to mongodb and elasticsearch
 */

@Service
@RequiredArgsConstructor
public class DataLoader {

    private static final Logger logger = LogManager.getLogger(DataLoader.class);
    private final ReferenceDictionary referenceDictionary;

    private final RestHighLevelClient client;

    private final Datastore datastore;

    @Value("${basePath}")
    private String basePath;

    @Value("${prod}")
    private boolean prod;

    /**
     * Elasticsearch indices
     */
    @Value("${indices}")
    private String[] indices;

    private static final String[] chrs = {
            "X", "Y", "M"
    };

    public void initData() throws IOException, InterruptedException {
        checkIndices();

        logger.info("Loading records");
        ExecutorService execService = Executors.newFixedThreadPool(8);
        List<Callable<Void>> tasks = new ArrayList<>();
        ;
        Callable<Void> clinvarTask = () -> {
            try {
                addClinvarRecords();
                logger.info("Finished adding Clinvar records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(clinvarTask);;
        Callable<Void> exacTask = () -> {
            try {
                addExacRecords();
                logger.info("Finished adding Exac records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(exacTask);
        Callable<Void> g1kTask = () -> {
            try {
                addG1kRecords();
                logger.info("Finished adding 1K records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(g1kTask);
        Callable<Void> varEffTask = () -> {
            try {
                addVarEffectRecords();
                logger.info("Finished adding Effect records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(varEffTask);
        Callable<Void> dbnsfpTask = () -> {
            try {
                addDBNSFPRecords();
                logger.info("Finished adding DBNSFP records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(dbnsfpTask);

        Callable<Void> acmgTask = () -> {
            try {
                addACMGRecords();
                logger.info("Finished adding ACMG Records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };
        tasks.add(acmgTask);

        Callable<Void> genesTask = () -> {
            try {
                addGeneRecord();
                logger.info("Finished adding Genes");
            } catch (Exception e) {
                logger.catching(e);
            }

            return null;
        };
        tasks.add(genesTask);

        Callable<Void> gnomadTask = () -> {
            try {
                addGnomadExomeRecords();
                logger.info("Finished adding Gnomad records");
            } catch (Exception e) {
                logger.catching(e);
            }
            return null;
        };

        tasks.add(gnomadTask);

        execService.invokeAll(tasks);
    }


    private void addClinvarRecords() throws IOException {
        Query<ClinVarRecord> query = datastore.createQuery(ClinVarRecord.class);
        if (query.count() == 0) {
            logger.info("Adding Clinvar Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "clinvar.vcf.gz") : PathUtil.join(basePath, "vcfs", "clinvar_sample.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);
                 BufferedReader reader = new BufferedReader(new FileReader(PathUtil.join(basePath, "vcfs", "var_citations.tsv"))
                 )) {
                Multimap<String, String> pubMap = ArrayListMultimap.create();
                reader.lines().forEach(s -> {
                    String[] cols = s.split("\\t");
                    pubMap.put(cols[0], cols[5]);
                });
                ClinVarVariantContextToRecordConverter recordConverter = new ClinVarVariantContextToRecordConverter();
                ClinVarRecord record = null;
                for (VariantContext variantContext : fileReader) {
                    record = recordConverter.convert(variantContext, referenceDictionary);
                    record.setPubmeds(pubMap.get(record.getAlleleId()));
                    datastore.save(record);
                }
            }
        }
    }

    private void addExacRecords() throws IOException {
        Query<ExacRecord> query = datastore.createQuery(ExacRecord.class);
        if (query.count() == 0) {
            logger.info("Adding Exac Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "exac.vcf.gz") : PathUtil.join(basePath, "vcfs", "exac_sample.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);) {
                ExacVariantContextToRecordConverter recordConverter = new ExacVariantContextToRecordConverter();
                for (VariantContext variantContext : fileReader) {
                    datastore.save(recordConverter.convert(variantContext, referenceDictionary));
                }
            }
        }
    }

    private void addGnomadExomeRecords() throws IOException {
        Query<GnomadExomeRecord> query = datastore.createQuery(GnomadExomeRecord.class);
        if(query.count() == 0) {
            logger.info("Adding Gnomad Exome Records");
            String fileName = prod ?  PathUtil.join(basePath, "vcfs", "gnomad_exomes.vcf.bgz") : PathUtil.join(basePath, "vcfs", "gnomad_exomes_sample.vcf.gz");
            try(VCFFileReader fileReader = new VCFFileReader(new File(fileName), false)) {
                GnomadExomeRecordConverter recordConverter = new GnomadExomeRecordConverter();
                for(VariantContext vc : fileReader) {
                    datastore.save(recordConverter.convert(vc, referenceDictionary));
                }

            }
        }
    }


    private void addVarEffectRecords() {
        Query<VariantEffectRecord> query = datastore.createQuery(VariantEffectRecord.class);
        if (query.count() == 0) {
            logger.info("Adding Variant Effect Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "var_effect.vcf.gz") : PathUtil.join(basePath, "vcfs", "var_effect_sample_ensembl.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);) {
                VariantContextToEffectRecordConverter recordConverter = new VariantContextToEffectRecordConverter();
                VariantEffectRecord record = null;
                for (VariantContext variantContext : fileReader) {
                    record = recordConverter.convert(variantContext, referenceDictionary);
                    if (record != null) {
                        datastore.save(record);
                    }
                }
            }
        }
    }

    private void addG1kRecords() {
        Query<ThousandGenomesRecord> query = datastore.createQuery(ThousandGenomesRecord.class);
        if (query.count() == 0) {
            logger.info("Adding 1000 Genome Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "1k.vcf.gz") : PathUtil.join(basePath, "vcfs", "1k_sample.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);) {
                ThousandGenomesVariantContextToRecordConverter recordConverter = new ThousandGenomesVariantContextToRecordConverter();
                for (VariantContext variantContext : fileReader) {
                    datastore.save(recordConverter.convert(variantContext, referenceDictionary));
                }
            }
        }
    }

    private void addDBNSFPRecords() throws IOException {
        Query<DBNSFPRecord> query = datastore.createQuery(DBNSFPRecord.class);
        if (query.count() == 0) {
            logger.info("Adding DBNSFP Records...");
            if (prod) {
                //Add from chr1-22
                for (int i = 1; i < 23; i++) {
                    logger.info("Adding Records for chr" + i);
                    String filename = PathUtil.join(basePath, "dbNSFP4c", String.format("dbNSFP4.0b2c_variant.chr%d.gz", i));
                    addDBNSFPRecord(filename);
                }
                //Add chr X, Y, M
                for (String chr : chrs) {
                    logger.info("Adding Records for chr" + chr);
                    String filename = PathUtil.join(basePath, "dbNSFP4c", String.format("dbNSFP4.0b2c_variant.chr%s.gz", chr));
                    addDBNSFPRecord(filename);
                }
            } else {
                addDBNSFPRecord(PathUtil.join(basePath, "vcfs", "dbNSFP_sample_chr1.tsv.gz"));
            }
        }

    }

    private void addDBNSFPRecord(String fileName) throws IOException {
        try (Reader decoder = new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)));
             BufferedReader reader = new BufferedReader(decoder)) {

            DBNSFPRecordConverter converter = new DBNSFPRecordConverter();
            DBNSFPRecord dbnsfpRecord = null;
            reader.readLine(); //read the columns
            String line = null;
            while ((line = reader.readLine()) != null) {
                DBNSFPRecord record = converter.convert(line, referenceDictionary);
                if (dbnsfpRecord != null) {
                    if (record.getChrom().equals(dbnsfpRecord.getChrom())
                            && record.getRef().equals(dbnsfpRecord.getRef()) && record.getPos() == dbnsfpRecord.getPos()) {
                        //If it is the same variant with d/t allele just update the scores
                        dbnsfpRecord.copy(record);
                    } else {
                        datastore.save(dbnsfpRecord);
                        dbnsfpRecord = record;
                    }
                } else {
                    dbnsfpRecord = record;
                    datastore.save(dbnsfpRecord);
                }
            }
        }
    }

    private void addGeneRecord() throws IOException {
        Query<GeneRecord> query = datastore.createQuery(GeneRecord.class);
        if (query.count() == 0) {
            logger.info("Adding Gene records");

            String fileName = PathUtil.join(basePath, "genes.tsv");

            try (CSVParser parser = CSVFormat.TDF.withHeader().parse(Files.newBufferedReader(Paths.get(fileName)))) {
                GeneRecordConverter converter = new GeneRecordConverter();
                for (CSVRecord record : parser.getRecords()) {
                    datastore.save(converter.convert(record, referenceDictionary));
                }

            }
        }
    }

    private void addACMGRecords() throws IOException {
        Query<ACMGRecord> query = datastore.createQuery(ACMGRecord.class);
        if(query.count() == 0){

            if(prod) {
                Files.list(Paths.get(basePath, "intervar")).forEach(path -> {
                    try (Reader decoder = new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile())));
                         BufferedReader reader = new BufferedReader(decoder)) {
                        logger.info("Adding ACMG Records");

                        ACMGRecordConverter converter = new ACMGRecordConverter();
                        reader.readLine(); //read the columns
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            datastore.save(converter.convert(line, referenceDictionary));
                        }
                    }
                     catch (IOException e) {
                        logger.catching(e);
                        logger.error("Couldn't load file: " + path.toString());
                    }
                });
            } else {
                String fileName = PathUtil.join(basePath, "vcfs", "demo_intervar.tsv.gz");
                try (Reader decoder = new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)));
                     BufferedReader reader = new BufferedReader(decoder)) {
                    logger.info("Adding ACMG Records");

                    ACMGRecordConverter converter = new ACMGRecordConverter();
                    reader.readLine(); //read the columns
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        datastore.save(converter.convert(line, referenceDictionary));
                    }
                }
            }
        }
    }


    /**
     * This method checks if an index exists and creates it if it doesn't
     */
    private void checkIndices() throws IOException {
        for (String index : indices) {
            GetIndexRequest getIndexRequest = new GetIndexRequest(index);
            getIndexRequest.local(false);
            getIndexRequest.includeDefaults(true);
            boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!exists) {
                logger.warn("The " + index + " index doesn't exist. Attempting to create it...");
                createIndex(index, client);
            }
        }

    }

    /**
     * Sends {@link CreateIndexRequest} to {@link RestHighLevelClient}
     *
     * @param index  - the index to be created
     * @param client - the ElasticSearch client
     * @throws IOException
     */
    private void createIndex(String index, RestHighLevelClient client) throws IOException {
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        String jsonMapping = FileUtils.readFileToString(FileUtils.getFile("src/main/resources/index_mappings", index + ".json"), "utf8");
        indexRequest.mapping(jsonMapping, XContentType.JSON);
        var response = client.indices().create(indexRequest, RequestOptions.DEFAULT);
        logger.info("Created " + response.index() + " index");
    }

}
