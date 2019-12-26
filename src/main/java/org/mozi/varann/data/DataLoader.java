package org.mozi.varann.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import htsjdk.samtools.util.IOUtil;
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
import org.mozi.varann.data.impl.annotation.VariantContextToEffectRecordConverter;
import org.mozi.varann.data.impl.clinvar.ClinVarVariantContextToRecordConverter;
import org.mozi.varann.data.impl.dbnsfp.DBNSFPRecordConverter;
import org.mozi.varann.data.impl.dbsnp.DBSNPVariantContextToRecordConverter;
import org.mozi.varann.data.impl.exac.ExacVariantContextToRecordConverter;
import org.mozi.varann.data.impl.g1k.ThousandGenomesVariantContextToRecordConverter;
import org.mozi.varann.data.records.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
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
        addClinvarRecords();
        addDBSNPRecords();
        addExacRecords();
        addG1kRecords();
        addVarEffectRecords();
        addDBNSFPRecords();

    }


    private void addClinvarRecords() throws IOException {
        Query<ClinVarRecord> query = datastore.createQuery(ClinVarRecord.class);
        if (query.count() == 0) {
            logger.info("Adding Clinvar Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "clinvar.vcf.gz") : PathUtil.join(basePath, "vcfs", "clinvar_sample.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);
                 BufferedReader reader = new BufferedReader(new FileReader(PathUtil.join(basePath, "vcfs", "var_citations.txt"))
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

    private void addDBSNPRecords() throws IOException {
        Query<DBSNPRecord> query = datastore.createQuery(DBSNPRecord.class);
        if (query.count() == 0) {
            logger.info("Addig DBSNP Records...");
            String fileName = prod ? PathUtil.join(basePath, "vcfs", "dbsnp.vcf.gz") : PathUtil.join(basePath, "vcfs", "dbsnp_sample.vcf");
            try (VCFFileReader fileReader = new VCFFileReader(new File(fileName), false);) {
                DBSNPVariantContextToRecordConverter recordConverter = new DBSNPVariantContextToRecordConverter();
                for (VariantContext variantContext : fileReader) {
                    datastore.save(recordConverter.convert(variantContext, referenceDictionary));
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
                    if(record != null) {
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
                    String filename = PathUtil.join(basePath, "dbNSFP4c", String.format("dbNSFP4.0b2c_variant.chr%d.gz", i));
                    addDBNSFPRecord(filename, query);
                }
                //Add chr X, Y, M
                for (String chr : chrs) {
                    String filename = PathUtil.join(basePath, "dbNSFP4c", String.format("dbNSFP4.0b2c_variant.chr%d.gz", chr));
                    addDBNSFPRecord(filename, query);
                }
            } else {
                addDBNSFPRecord(PathUtil.join(basePath, "vcfs", "dbNSFP_sample_chr1.tsv"), query);
            }
        }

    }

    private void addDBNSFPRecord(String fileName, Query<DBNSFPRecord> query) throws IOException {
        try (Reader decoder = new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)));
             BufferedReader reader = new BufferedReader(decoder);
             CSVParser parser = CSVFormat.TDF.withHeader().parse(reader)) {

            DBNSFPRecordConverter converter = new DBNSFPRecordConverter();
            DBNSFPRecord dbsnpRecord = null;
            for (CSVRecord csvRecord : parser.getRecords()) {
                DBNSFPRecord record = converter.convert(csvRecord, referenceDictionary);
                if (dbsnpRecord != null && record.getChrom().equals(dbsnpRecord.getChrom())
                        && record.getRef().equals(dbsnpRecord.getRef()) && record.getPos() == dbsnpRecord.getPos()) {
                    //If it is the same variant with d/t allele just update the scores
                    dbsnpRecord.copy(record);
                    UpdateOperations<DBNSFPRecord> updateOp = datastore.createUpdateOperations(DBNSFPRecord.class)
                            .set("alt", dbsnpRecord.getAlt()).set("hgvs", dbsnpRecord.getHgvs())
                            .set("sift", dbsnpRecord.getSift())
                            .set("cadd", dbsnpRecord.getCadd()).set("polyphen2", dbsnpRecord.getPolyphen2())
                            .set("lrt", dbsnpRecord.getLrt()).set("mutationTaster", dbsnpRecord.getMutationTaster())
                            .set("dann", dbsnpRecord.getMutationTaster()).set("vest4", dbsnpRecord.getVest4());

                    datastore.update(query, updateOp);
                } else {
                    dbsnpRecord = record;
                    datastore.save(dbsnpRecord);
                }
            }
        }
    }


    /**
     * This method checks if an index exists and creates it if it doesn't
     */
    private void checkIndices() throws IOException {
        for (int i = 0; i < indices.length; i++) {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indices[i]);
            getIndexRequest.local(false);
            getIndexRequest.includeDefaults(true);
            boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!exists) {
                logger.warn("The " + indices[i] + " index doesn't exist. Attempting to create it...");
                createIndex(indices[i], client);
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
