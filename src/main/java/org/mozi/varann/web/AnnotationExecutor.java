package org.mozi.varann.web;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import dev.morphia.Datastore;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.mozi.varann.data.VariantInfo;
import org.mozi.varann.data.records.VariantEffectRecord;
import org.mozi.varann.data.records.ClinVarRecord;
import org.mozi.varann.data.records.ExacRecord;
import org.mozi.varann.data.records.ThousandGenomesRecord;
import org.mozi.varann.util.AnnotationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class executes the annotation request
 */
@Service
@RequiredArgsConstructor
public class AnnotationExecutor {

    private final RestHighLevelClient client;

    private final Datastore datastore;

    @Value("${indices}")
    private String[] indices;


    public VariantInfo annotateId(String id) throws AnnotationException, IOException {
        if(id.contains("rs")) {
            id = id.substring(id.indexOf("rs") + 2);
        }

        //Query dbSNP for the id;
        //TODO Handle the case for rsid with multiple alleles
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"dbsnp"},"rsId" ,id), RequestOptions.DEFAULT);

        if(searchResponse.getHits().getTotalHits().value == 0){ //RsId not found
            throw new AnnotationException("Couldn't find a variant with id " + id);
        }

        SearchHit hit =  searchResponse.getHits().getAt(0);
        String hgvs = ((ArrayList<String>)hit.getSourceAsMap().get("hgvs")).get(0);

        searchResponse = client.search(getSearchRequest(indices,"hgvs", hgvs), RequestOptions.DEFAULT);

        return buildVariantInfo(searchResponse.getHits());


    }

    public VariantInfo annotateHgvs(String hgvs) throws AnnotationException, IOException {
        var searchResponse = client.search(getSearchRequest(new String[]{"dbsnp"},"hgvs", hgvs), RequestOptions.DEFAULT);
        if(searchResponse.getHits().getTotalHits().value == 0){ //RsId not found
            throw new AnnotationException("Couldn't find a variant with hgvs id " + hgvs);
        }
        searchResponse = client.search(getSearchRequest(indices,"hgvs", hgvs), RequestOptions.DEFAULT);
        return buildVariantInfo(searchResponse.getHits());
    }

    public List<VariantInfo> annotateByGene(String gene, int from, int size) throws AnnotationException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"dbsnp"},"genes.symbol" ,gene, from, size), RequestOptions.DEFAULT);

        if(searchResponse.getHits().getTotalHits().value == 0){ //RsId not found
            throw new AnnotationException("Couldn't find a variant occurring in a gene " + gene);
        }

        List<VariantInfo> varInfos = new ArrayList<>();

        for(SearchHit hit : searchResponse.getHits()){

            List<String> hgvs = ((ArrayList<String>)hit.getSourceAsMap().get("hgvs"));
            for(var hgv : hgvs) {
                searchResponse = client.search(getSearchRequest(indices,"hgvs", hgv), RequestOptions.DEFAULT);
                varInfos.add(buildVariantInfo(searchResponse.getHits()));
            }


        }

        return varInfos;
    }

    private SearchRequest getSearchRequest(String[] indices, String field, String value) {
        return getSearchRequest(indices, field, value, 0, 10);
    }

    private SearchRequest getSearchRequest(String[] indices, String field, String value, int from, int size) {
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        var boolQuery = QueryBuilders.boolQuery();
        boolQuery.must().add(QueryBuilders.termQuery(field, value));
        sourceBuilder.query(boolQuery);
        sourceBuilder.from(from);
        sourceBuilder.size(size);;
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    private VariantInfo buildVariantInfo(SearchHits hits) {
        VariantInfo varInfo = new VariantInfo();
        for(var hit : hits){
            String index = hit.getIndex();
            String id = hit.getId();
            switch (index) {
                case "dbsnp":
                    varInfo.setChrom((String)hit.getSourceAsMap().get("chrom"));
                    varInfo.setPos((int)hit.getSourceAsMap().get("pos"));
                    varInfo.setId((String)hit.getSourceAsMap().get("rsId"));
                    varInfo.setRef((String)hit.getSourceAsMap().get("ref"));
                    String alt = ((ArrayList<String>)hit.getSourceAsMap().get("alt")).get(0);
                    String hgvs = ((ArrayList<String>)hit.getSourceAsMap().get("hgvs")).get(0);
                    varInfo.setAlt(alt);
                    varInfo.setHgvs(hgvs);
                    break;
                case "clinvar":
                    var clinvarQuery = datastore.createQuery(ClinVarRecord.class);
                    ClinVarRecord clinvarRec = clinvarQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setClinvar(clinvarRec);
                    break;
                case "exac":
                    var exacQuery = datastore.createQuery(ExacRecord.class);
                    ExacRecord exacRec = exacQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setExac(exacRec);
                    break;
                case "effect":
                    var effectQuery = datastore.createQuery(VariantEffectRecord.class);
                    VariantEffectRecord effectRec = effectQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setEffect(effectRec);
                    break;

                case "g1k":
                    var g1kQuery = datastore.createQuery(ThousandGenomesRecord.class);
                    ThousandGenomesRecord g1kRec = g1kQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setThousandGenome(g1kRec);
                    break;
            }
        }

        return varInfo;
    }
}
