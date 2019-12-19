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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.xcontent.XContentType;
import org.mozi.varann.data.impl.annotation.VariantContextToEffectRecordConverter;
import org.mozi.varann.data.records.VariantEffectRecord;
import org.mozi.varann.data.records.ClinVarRecord;
import org.mozi.varann.data.impl.clinvar.ClinVarVariantContextToRecordConverter;
import org.mozi.varann.data.records.DBSNPRecord;
import org.mozi.varann.data.impl.dbsnp.DBSNPVariantContextToRecordConverter;
import org.mozi.varann.data.records.ExacRecord;
import org.mozi.varann.data.impl.exac.ExacVariantContextToRecordConverter;
import org.mozi.varann.data.records.ThousandGenomesRecord;
import org.mozi.varann.data.impl.g1k.ThousandGenomesVariantContextToRecordConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
     *
     */
    @Value("${indices}")
    private String[] indices;


    public void initData() throws IOException {
        checkIndices();
        logger.info("Loading records");
        addClinvarRecords();
        addDBSNPRecords();
        addExacRecords();
        addG1kRecords();
        addVarEffectRecords();
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
                for (VariantContext variantContext : fileReader) {
                    datastore.save(recordConverter.convert(variantContext, referenceDictionary));
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


    /**
     * This method checks if an index exists and creates it if it doesn't
     *
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
