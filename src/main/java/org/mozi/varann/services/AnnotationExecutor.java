package org.mozi.varann.services;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

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
import org.elasticsearch.search.SearchHit;
import org.mozi.varann.data.records.GeneRecord;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.web.data.GeneInfo;
import org.mozi.varann.web.data.VariantInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mozi.varann.util.SearchUtils.*;

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

    private static final Logger logger = LogManager.getLogger(AnnotationExecutor.class);
    @SuppressWarnings("unchecked")
    public VariantInfo annotateId(String id) throws AnnotationException, IOException {
        if (id.contains("rs")) {
            id = id.substring(id.indexOf("rs") + 2);
        }

        //Query dbSNP for the id;
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"effect"}, "rsId", id), RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a variant with id " + id);
        }
        if (searchResponse.getHits().getTotalHits().value > 1) {
            throw new AnnotationException("Multiple variants found for id " + id);
        }
        SearchHit hit = searchResponse.getHits().getAt(0);
        List<String> hgvs = ((ArrayList<String>) hit.getSourceAsMap().get("hgvs"));
        if (hgvs.size() > 1) {
            throw new AnnotationException("More than one allele found for this variant " + hgvs);
        }

        searchResponse = client.search(getSearchRequest(indices, "hgvs", hgvs.get(0)), RequestOptions.DEFAULT);

        VariantInfo variantInfo = buildVariantInfo(datastore ,searchResponse.getHits(), hgvs.get(0));

        return setVariantGene(client,variantInfo);
    }

    public VariantInfo annotateHgvs(String hgvs) throws AnnotationException, IOException {

        var searchResponse = client.search(getSearchRequest(indices, "hgvs", hgvs), RequestOptions.DEFAULT);
        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a variant with hgvs id " + hgvs);
        }
        VariantInfo variantInfo = buildVariantInfo(datastore ,searchResponse.getHits(), hgvs);

        return setVariantGene(client,variantInfo);
    }

    /**
     * Asynchronously annotate a list of variants
     */
    @Async
    public CompletableFuture<List<VariantInfo>> annotateMultipleVariants(List<String> ids) throws AnnotationException, IOException {
        logger.info(String.format("Received %d variants", ids.size()));
        List<VariantInfo> result = new ArrayList<>();
        for(String id: ids){
            result.add(annotateId(id));
        }

        return CompletableFuture.completedFuture(result);
    }


    @SuppressWarnings("unchecked")
    public List<VariantInfo> annotateByRange(String chr, long start, long end) throws IOException {
        SearchRequest request = getRangeRequest(new String[]{"clinvar", "effect", "exac", "g1k", "dbnsfp"}, chr, start, end);
        var searchResponse = client.search(request, RequestOptions.DEFAULT);
        if (searchResponse.getHits().getTotalHits().value == 0) {
            return new ArrayList<>();
        }

        List<VariantInfo> varInfos = new ArrayList<>();
        for (SearchHit varHit : searchResponse.getHits()) {
            List<String> hgvs = ((ArrayList<String>) varHit.getSourceAsMap().get("hgvs"));
            if (hgvs.size() > 1) {
                for (var hgv : hgvs) {
                    varInfos.add(setVariantGene(client ,buildVariantInfo(datastore,varHit, hgv)));
                }
            } else {
                varInfos.add(setVariantGene(client ,buildVariantInfo(datastore,varHit, hgvs.get(0))));
            }
        }
        return varInfos;
    }
}
