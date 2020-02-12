package org.mozi.varann.services;

import dev.morphia.Datastore;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.mozi.varann.data.records.TranscriptRecord;
import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.web.models.VariantInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mozi.varann.util.SearchUtils.buildVariantInfo;
import static org.mozi.varann.util.SearchUtils.getSearchRequest;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/29/20
 */
@Service
@RequiredArgsConstructor
public class GeneAnnotationExecutor {

    private final RestHighLevelClient client;

    private final Datastore datastore;

    @Value("${indices}")
    private String[] indices;

    private static final Logger logger = LogManager.getLogger(GeneAnnotationExecutor.class);

    @SuppressWarnings("unchecked")
    public List<VariantInfo> annotateByGene(String gene) throws AnnotationNotFoundException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"intervar", "variant"}, "gene", gene), RequestOptions.DEFAULT);

        int totalHits = (int) searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a gene with symbol " + gene);
        }
        List<VariantInfo> variantInfos = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            variantInfos.add(buildVariantInfo(datastore, hit, (String) hit.getSourceAsMap().get("hgvs")));
        }

        return variantInfos;
    }

    @SuppressWarnings("unchecked")
    public List<VariantInfo> annotateGeneById(String id) throws AnnotationNotFoundException, IOException {

        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"variant", "intervar"}, "geneId", id), RequestOptions.DEFAULT);

        int totalHits = (int) searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a gene with id " + id);
        }
        List<VariantInfo> variantInfos = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            variantInfos.add(buildVariantInfo(datastore, hit, (String)hit.getSourceAsMap().get("hgvs")));
        }

        return variantInfos;
    }

    public List<TranscriptRecord> getTranscriptsInRange(String contig, long start, long end) throws IOException {
        List<TranscriptRecord> transcripts = new ArrayList<>();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("txStart").lte(end))
                .must(QueryBuilders.rangeQuery("txEnd").gte(start))
                .must(QueryBuilders.matchQuery("chrom", contig));

        SearchRequest request = new SearchRequest("transcript");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        if (response.getHits().getTotalHits().value == 0) {
            return transcripts;
        }

        for (SearchHit hit : response.getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            TranscriptRecord transcript = new TranscriptRecord();
            transcript.setName((String) map.get("name"));
            ;
            transcript.setId((String) map.get("id"));
            transcript.setGeneId((String) map.get("geneId"));
            transcript.setChrom((String) map.get("chrom"));
            transcript.setTxStart(Long.valueOf((Integer) map.get("txStart")));
            transcript.setTxEnd(Long.valueOf((Integer) map.get("txEnd")));

            transcripts.add(transcript);
        }
        return transcripts;
    }

    public List<TranscriptRecord> getTranscriptsByGene(String geneId) throws IOException {
        List<TranscriptRecord> transcripts = new ArrayList<>();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("geneId", geneId));

        SearchRequest request = new SearchRequest("transcript");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        if (response.getHits().getTotalHits().value == 0) {
            return transcripts;
        }

        for (SearchHit hit : response.getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            TranscriptRecord transcript = new TranscriptRecord();
            transcript.setName((String) map.get("name"));
            ;
            transcript.setId((String) map.get("id"));
            transcript.setGeneId((String) map.get("geneId"));
            transcript.setChrom((String) map.get("chrom"));
            transcript.setTxStart(Long.valueOf((Integer) map.get("txStart")));
            transcript.setTxEnd(Long.valueOf((Integer) map.get("txEnd")));

            transcripts.add(transcript);
        }
        return transcripts;
    }
}
