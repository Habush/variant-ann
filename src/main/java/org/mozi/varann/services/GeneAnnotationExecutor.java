package org.mozi.varann.services;

import com.google.common.base.Joiner;
import dev.morphia.Datastore;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.mozi.varann.data.records.GeneRecord;
import org.mozi.varann.data.records.TranscriptRecord;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.util.MultipleValuesException;
import org.mozi.varann.web.models.GeneInfo;
import org.mozi.varann.web.models.VariantInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mozi.varann.util.SearchUtils.*;

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

    public GeneInfo annotateByGene(String gene) throws AnnotationNotFoundException, MultipleValuesException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "symbol", gene), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a gene with symbol " + gene);
        }

        if (totalHits > 1) {
            List<String> idBuf = new ArrayList<>();
            for(int i = 0; i < totalHits; i++) {
               idBuf.add((String)searchResponse.getHits().getAt(i).getSourceAsMap().get("id"));
            }

            throw new MultipleValuesException(gene, idBuf);
        }

        GeneInfo geneInfo =  getGeneInfo(gene, searchResponse);
        geneInfo.setTranscripts(getTranscriptsByGene(geneInfo.getId()));

        return geneInfo;
    }

    public GeneInfo annotateByEntrezId(String entrezId) throws AnnotationNotFoundException, MultipleValuesException ,IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "entrezID", entrezId), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a gene with entrezID " + entrezId);
        }

        if (totalHits > 1) {
            List<String> idBuf = new ArrayList<>();
            for(int i = 0; i < totalHits; i++) {
                idBuf.add((String)searchResponse.getHits().getAt(i).getSourceAsMap().get("id"));
            }
            throw new MultipleValuesException(entrezId, idBuf);
        }
        GeneInfo geneInfo =  getGeneInfo(entrezId, searchResponse);
        geneInfo.setTranscripts(getTranscriptsByGene(geneInfo.getId()));

        return geneInfo;
    }

    public GeneInfo annotateGeneById(String id) throws AnnotationNotFoundException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "id", id), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a gene with ensemble id " + id);
        }

        if (totalHits > 1) {
            //TODO this needs to improved!
            //Just get the first id and use that
            return getGeneInfo((String) searchResponse.getHits().getAt(0).getSourceAsMap().get("id"), searchResponse);
        }
        GeneInfo geneInfo =  getGeneInfo(id, searchResponse);
        geneInfo.setTranscripts(getTranscriptsByGene(geneInfo.getId()));

        return geneInfo;
    }

    public List<GeneInfo> getGenesInRange(String contig, long start, long end) throws IOException {
        List<GeneInfo> genes = new ArrayList<>();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("start").lte((int)end))
                    .must(QueryBuilders.rangeQuery("end").gte((int)start))
                    .must(QueryBuilders.matchQuery("chrom", contig));

        SearchRequest request = new SearchRequest("genes");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        if (response.getHits().getTotalHits().value == 0){
            return genes;
        }

        for (SearchHit hit: response.getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            GeneInfo geneInfo = new GeneInfo();
            geneInfo.setName((String) map.get("name"));
            geneInfo.setSymbol((String)map.get("symbol"));
            geneInfo.setType((String)map.get("type"));
            geneInfo.setId((String)map.get("id"));
            geneInfo.setEntrezId((String)map.get("entrezId"));
            geneInfo.setStart(Long.valueOf((Integer) map.get("start")));
            geneInfo.setEnd(Long.valueOf((Integer) map.get("end")));

            genes.add(geneInfo);
        }

        return genes;
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

        if (response.getHits().getTotalHits().value == 0){
            return transcripts;
        }

        for (SearchHit hit: response.getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            TranscriptRecord transcript = new TranscriptRecord();
            transcript.setName((String) map.get("name"));;
            transcript.setId((String)map.get("id"));
            transcript.setGeneId((String)map.get("geneId"));
            transcript.setChrom((String)map.get("chrom"));
            transcript.setTxStart(Long.valueOf((Integer)map.get("txStart")));
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

        if (response.getHits().getTotalHits().value == 0){
            return transcripts;
        }

        for (SearchHit hit: response.getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            TranscriptRecord transcript = new TranscriptRecord();
            transcript.setName((String) map.get("name"));;
            transcript.setId((String)map.get("id"));
            transcript.setGeneId((String)map.get("geneId"));
            transcript.setChrom((String)map.get("chrom"));
            transcript.setTxStart(Long.valueOf((Integer)map.get("txStart")));
            transcript.setTxEnd(Long.valueOf((Integer) map.get("txEnd")));

            transcripts.add(transcript);
        }
        return transcripts;
    }



    @SuppressWarnings("unchecked")
    private GeneInfo getGeneInfo(String entrezId, SearchResponse searchResponse) throws IOException {
        List<VariantInfo> varInfos = new ArrayList<>();
        SearchHit hit = searchResponse.getHits().getAt(0);
        long start = (Integer) hit.getSourceAsMap().get("start");
        long end = (Integer) hit.getSourceAsMap().get("end");
        String contig = (String) hit.getSourceAsMap().get("chrom");

        SearchRequest searchRequest = getRangeRequest(new String[]{"clinvar", "effect", "exac", "g1k", "dbnsfp", "gnomad_exome", "acmg", }, contig, start, end);
        SearchResponse varResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        if (varResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a variants for gene " + entrezId);
        }
        for (SearchHit varHit : varResponse.getHits()) {
            List<String> hgvs = ((ArrayList<String>) varHit.getSourceAsMap().get("hgvs"));
            if (hgvs.size() > 1) {
                for (var hgv : hgvs) {
                    varInfos.add(buildVariantInfo(datastore ,varHit, hgv));
                }
            } else {
                varInfos.add(buildVariantInfo(datastore ,varHit, hgvs.get(0)));
            }
        }

        GeneInfo geneInfo = buildGeneInfo(hit);
        varInfos.forEach(info -> info.setGene(geneInfo.getSymbol()));
        geneInfo.setVariants(varInfos);
        return geneInfo;
    }

    private GeneInfo buildGeneInfo(SearchHit hit) {
        String id = hit.getId();
        var geneQuery = datastore.createQuery(GeneRecord.class);
        var geneRecord = geneQuery.field("_id").equal(new ObjectId(id)).find().next();

        GeneInfo geneInfo = new GeneInfo();
        geneInfo.setSymbol(geneRecord.getSymbol());
        geneInfo.setEntrezId(geneRecord.getEntrezID());
        geneInfo.setId(geneRecord.getId());
        geneInfo.setType(geneRecord.getType());
        geneInfo.setName(geneRecord.getName());

        return geneInfo;
    }
}
