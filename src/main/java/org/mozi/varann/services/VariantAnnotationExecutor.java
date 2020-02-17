package org.mozi.varann.services;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import com.google.common.collect.Lists;
import dev.morphia.Datastore;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.util.MultipleValuesException;
import org.mozi.varann.web.models.MultipleVariantResult;
import org.mozi.varann.web.models.VariantInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.mozi.varann.util.SearchUtils.*;

/**
 * This class executes the annotation request
 */
@Service
@RequiredArgsConstructor
public class VariantAnnotationExecutor {

    private final RestHighLevelClient client;

    private final Datastore datastore;

    @Value("${indices}")
    private String[] indices;

    private static final Logger logger = LogManager.getLogger(VariantAnnotationExecutor.class);

    @SuppressWarnings("unchecked")
    public VariantInfo annotateId(String id) throws AnnotationNotFoundException, MultipleValuesException, IOException {
        //Query dbSNP for the id;
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"variant"}, new String[]{"rsId"}, new String[]{id}), RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a variant with id " + id);
        }
        SearchHit hit = searchResponse.getHits().getAt(0);
        String hgvs = (String) hit.getSourceAsMap().get("hgvs");

        searchResponse = client.search(getSearchRequest(indices, new String[]{"hgvs"}, new String[]{hgvs}), RequestOptions.DEFAULT);

        return buildVariantInfo(datastore, searchResponse.getHits(), hgvs);
    }

    public VariantInfo annotateHgvs(String hgvs) throws AnnotationNotFoundException, IOException {

        var searchResponse = client.search(getSearchRequest(indices, new String[]{"hgvs"}, new String[]{hgvs}), RequestOptions.DEFAULT);
        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationNotFoundException("Couldn't find a variant with hgvs id " + hgvs);
        }

        return buildVariantInfo(datastore, searchResponse.getHits(), hgvs);
    }

    public VariantInfo annotateChangeString(String chrom, String pos, String ref, String alt) throws AnnotationNotFoundException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"variant"}, new String[]{"chrom", "pos", "ref", "alt"}, new String[]{chrom, pos, ref, alt}), RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationNotFoundException(String.format("Couldn't find a variant  %s:%s:%s:%s", chrom, pos, ref, alt));
        }
        ;

        return annotateHgvs((String) searchResponse.getHits().getAt(0).getSourceAsMap().get("hgvs"));
    }

    /**
     * Asynchronously annotate a list of variants
     */
    @Async
    public CompletableFuture<MultipleVariantResult> annotateMultipleVariants(List<String> ids) throws IOException {
        logger.info(String.format("Received %d variants", ids.size()));
        List<String> notFound = new ArrayList<>();
        Map<String, List<String>> multiVals = new HashMap<>();
        List<VariantInfo> result = new ArrayList<>();
        for (String id : ids) {
            try {
                result.add(annotateId(id));
            } catch (AnnotationNotFoundException ex) {
                notFound.add(id);
            } catch (MultipleValuesException ex) {
                multiVals.put(ex.getReq(), ex.getValues());
            }

        }

        return CompletableFuture.completedFuture(new MultipleVariantResult(result, notFound, multiVals));
    }


    @SuppressWarnings("unchecked")
    public List<VariantInfo> annotateByRange(String chr, long start, long end, int limit) throws IOException {
        SearchRequest request = getRangeRequest(new String[]{ "variant", "intervar"}, chr, start, end);
        request.source().size(limit);
        var searchResponse = client.search(request, RequestOptions.DEFAULT);
        if (searchResponse.getHits().getTotalHits().value == 0) {
            return new ArrayList<>();
        }

        List<VariantInfo> varInfos = new ArrayList<>();
        for (SearchHit varHit : searchResponse.getHits()) {
            if (varHit.getSourceAsMap().get("hgvs") instanceof ArrayList) {
                List<String> hgvs = ((ArrayList<String>) varHit.getSourceAsMap().get("hgvs"));
                if (hgvs.size() > 1) {
                    for (var hgv : hgvs) {
                        varInfos.add(buildVariantInfo(datastore, varHit, hgv));
                    }
                } else {
                    varInfos.add(buildVariantInfo(datastore, varHit, hgvs.get(0)));
                }
            } else {
                varInfos.add(buildVariantInfo(datastore, varHit, (String)varHit.getSourceAsMap().get("hgvs")));
            }

        }
        return varInfos;
    }
}
