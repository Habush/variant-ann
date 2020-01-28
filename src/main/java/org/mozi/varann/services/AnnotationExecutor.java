package org.mozi.varann.services;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import com.google.common.base.Joiner;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
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
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.mozi.varann.data.records.*;
import org.mozi.varann.util.AnnotationException;
import org.mozi.varann.web.data.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        VariantInfo variantInfo = buildVariantInfo(searchResponse.getHits(), hgvs.get(0));

        return setVariantGene(variantInfo);
    }

    public VariantInfo annotateHgvs(String hgvs) throws AnnotationException, IOException {

        var searchResponse = client.search(getSearchRequest(indices, "hgvs", hgvs), RequestOptions.DEFAULT);
        if (searchResponse.getHits().getTotalHits().value == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a variant with hgvs id " + hgvs);
        }
        VariantInfo variantInfo = buildVariantInfo(searchResponse.getHits(), hgvs);

        return setVariantGene(variantInfo);
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

    public GeneInfo annotateByGene(String gene) throws AnnotationException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "symbol", gene), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a gene with symbol " + gene);
        }

        if (totalHits > 1) {
            String[] idBuf = new String[totalHits];
            for(int i = 0; i < totalHits; i++) {
                idBuf[i] =  (String)searchResponse.getHits().getAt(i).getSourceAsMap().get("id");
            }
            String msg = String.format("Found more than one gene for symbol %s with the following ids: %s", gene, Joiner.on(",").join(idBuf));
            throw new AnnotationException(msg);
        }

        return getGeneInfo(gene, searchResponse);
    }

    public GeneInfo annotateByEntrezId(String entrezId) throws AnnotationException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "entrezID", entrezId), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a gene with entrezID " + entrezId);
        }

        if (totalHits > 1) {
            String[] idBuf = new String[totalHits];
            for(int i = 0; i < totalHits; i++) {
                idBuf[i] =  (String)searchResponse.getHits().getAt(i).getSourceAsMap().get("id");
            }
            String msg = String.format("Found more than one gene for entrezID %s with the following ids: %s", entrezId, Joiner.on(",").join(idBuf));
            throw new AnnotationException(msg);
        }
        return getGeneInfo(entrezId, searchResponse);
    }

    public GeneInfo annotateGeneById(String id) throws AnnotationException, IOException {
        SearchResponse searchResponse = client.search(getSearchRequest(new String[]{"genes"}, "id", id), RequestOptions.DEFAULT);

        int totalHits = (int)searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) { //RsId not found
            throw new AnnotationException("Couldn't find a gene with ensemble id " + id);
        }

        if (totalHits > 1) {
            //TODO this needs to improved!
           //Just get the first id and use that
            return getGeneInfo((String) searchResponse.getHits().getAt(0).getSourceAsMap().get("id"), searchResponse);
        }
        return getGeneInfo(id, searchResponse);
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
                    varInfos.add(buildVariantInfo(varHit, hgv));
                }
            } else {
                varInfos.add(buildVariantInfo(varHit, hgvs.get(0)));
            }
        }

        GeneInfo geneInfo = buildGeneInfo(hit);
        varInfos.forEach(info -> info.setGene(geneInfo.getSymbol()));
        geneInfo.setVariants(varInfos);
        return geneInfo;
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
                    varInfos.add(setVariantGene(buildVariantInfo(varHit, hgv)));
                }
            } else {
                varInfos.add(setVariantGene(buildVariantInfo(varHit, hgvs.get(0))));
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
        sourceBuilder.size(size);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    private SearchRequest getRangeRequest(String[] indices, String contig, long start, long end) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("pos");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("chrom", contig);
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(rangeQueryBuilder);
        sourceBuilder.postFilter(matchQueryBuilder);
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    @SuppressWarnings("unchecked")
    private VariantInfo buildVariantInfo(SearchHits hits, String hgvs) {
        VariantInfo varInfo = new VariantInfo();
        for (var hit : hits) {
            String index = hit.getIndex();
            String id = hit.getId();
            int hgvsIndex = ((ArrayList<String>) hit.getSourceAsMap().get("hgvs")).indexOf(hgvs);
            String alt = ((ArrayList<String>) hit.getSourceAsMap().get("alt")).get(hgvsIndex);
            if (varInfo.getAlt() == null) {
                varInfo.setAlt(alt);
                varInfo.setHgvs(hgvs);
            }
            switch (index) {
                case "clinvar":
                    var clinvarQuery = datastore.createQuery(ClinVarRecord.class);
                    ClinVarRecord clinvarRec = clinvarQuery.field("_id").equal(new ObjectId(id)).find().next();
                    DiseaseInfo diseaseInfonfo = new DiseaseInfo();
                    diseaseInfonfo.setAnnotation(clinvarRec.getAnnotations().get(hgvsIndex));
                    diseaseInfonfo.setPumeds(clinvarRec.getPubmeds());
                    varInfo.setClinvar(diseaseInfonfo);
                    break;
                case "exac":
                    var exacQuery = datastore.createQuery(ExacRecord.class);
                    ExacRecord exacRec = exacQuery.field("_id").equal(new ObjectId(id)).find().next();
                    for(ExacPopulation pop : ExacPopulation.values()){
                        PopulationData data = new PopulationData();
                        data.setAC(exacRec.getAlleleCounts().containsKey(pop) ? exacRec.getAlleleCounts().get(pop).get(hgvsIndex): -1);
                        data.setAF(exacRec.getAlleleFrequencies().containsKey(pop) ? exacRec.getAlleleFrequencies().get(pop).get(hgvsIndex) : -1);
                        data.setAN(exacRec.getChromCounts().getOrDefault(pop, -1));
                        data.setHomAlt(exacRec.getAlleleHomCounts().containsKey(pop) ? exacRec.getAlleleHomCounts().get(pop).get(hgvsIndex) : -1);
                        if(varInfo.getPopulation().get(pop.name()) == null) {
                            PopulationInfo info = new PopulationInfo();
                            varInfo.getPopulation().put(pop.name(), info);
                        }
                        varInfo.getPopulation().get(pop.name()).setExac(data);
                    }
                    break;
                case "effect":
                    var effectQuery = datastore.createQuery(VariantEffectRecord.class);
                    VariantEffectRecord effectRec = effectQuery.field("_id").equal(new ObjectId(id)).find().next();
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.setAnnotation(effectRec.getAnnotation().get(alt));
                    effectInfo.setHgvsNomination(effectRec.getHgvsNomination().get(alt));
                    varInfo.setEffect(effectInfo);
                    varInfo.setId(effectRec.getRsId());
                    break;

                case "g1k":
                    var g1kQuery = datastore.createQuery(ThousandGenomesRecord.class);
                    ThousandGenomesRecord g1kRec = g1kQuery.field("_id").equal(new ObjectId(id)).find().next();
                    for(ThousandGenomesPopulation pop: ThousandGenomesPopulation.values()){
                        PopulationData data = new PopulationData();
                        data.setAC(g1kRec.getAlleleCounts().containsKey(pop)  ? g1kRec.getAlleleCounts().get(pop).get(hgvsIndex) : -1);
                        data.setAF(g1kRec.getAlleleFrequencies().containsKey(pop) ?  g1kRec.getAlleleFrequencies().get(pop).get(hgvsIndex): -1);
                        data.setAN(g1kRec.getChromCounts().getOrDefault(pop, -1));
                        if(varInfo.getPopulation().get(pop.name()) == null) {
                            PopulationInfo info = new PopulationInfo();
                            varInfo.getPopulation().put(pop.name(), info);
                        }
                        varInfo.getPopulation().get(pop.name()).setThousandGenome(data);
                    }
                    break;
                case "dbnsfp":
                    var dbnsfpQuery = datastore.createQuery(DBNSFPRecord.class);
                    DBNSFPRecord dbnsfpRec = dbnsfpQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setScores(getScoreInfo(hgvs, dbnsfpRec));
                    break;
                case "acmg":
                    var acmgQuery = datastore.createQuery(ACMGRecord.class);
                    ACMGRecord acmgRecord = acmgQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setAcmg(acmgRecord);
                    break;
                case "gnomad_exome":
                    var gnomadQuery = datastore.createQuery(GnomadExomeRecord.class);
                    GnomadExomeRecord gnomadExomeRecord = gnomadQuery.field("_id").equal(new ObjectId(id)).find().next();
                    for(GnomadExomePopulation pop : GnomadExomePopulation.values()){
                        PopulationData data = new PopulationData();
                        data.setAC(gnomadExomeRecord.getAlleleCounts().containsKey(pop) ? gnomadExomeRecord.getAlleleCounts().get(pop).get(hgvsIndex) : -1);
                        data.setAF(gnomadExomeRecord.getAlleleFrequencies().containsKey(pop) ? gnomadExomeRecord.getAlleleFrequencies().get(pop).get(hgvsIndex): -1);
                        data.setAN(gnomadExomeRecord.getChromCounts().getOrDefault(pop, -1));
                        data.setHomAlt(gnomadExomeRecord.getAlleleHomCounts().containsKey(pop) ? gnomadExomeRecord.getAlleleHomCounts().get(pop).get(hgvsIndex) : -1);
                        if(varInfo.getPopulation().get(pop.name()) == null) {
                            PopulationInfo info = new PopulationInfo();
                            varInfo.getPopulation().put(pop.name(), info);
                        }
                        varInfo.getPopulation().get(pop.name()).setGnomadExome(data);
                    }
                    break;
            }

            if (varInfo.getRef() == null) {
                setRecord(varInfo, hit);
            }
        }

        return varInfo;
    }

    @SuppressWarnings("unchecked")
    private VariantInfo buildVariantInfo(SearchHit hit, String hgvs) {
        VariantInfo varInfo = new VariantInfo();
        String index = hit.getIndex();
        String id = hit.getId();
        int hgvsIndex = ((ArrayList<String>) hit.getSourceAsMap().get("hgvs")).indexOf(hgvs);
        String alt = ((ArrayList<String>) hit.getSourceAsMap().get("alt")).get(hgvsIndex);
        if (varInfo.getAlt() == null) {
            varInfo.setAlt(alt);
            varInfo.setHgvs(hgvs);
        }
        switch (index) {
            case "clinvar":
                var clinvarQuery = datastore.createQuery(ClinVarRecord.class);
                ClinVarRecord clinvarRec = clinvarQuery.field("_id").equal(new ObjectId(id)).find().next();
                DiseaseInfo diseaseInfonfo = new DiseaseInfo();
                diseaseInfonfo.setAnnotation(clinvarRec.getAnnotations().get(hgvsIndex));
                diseaseInfonfo.setPumeds(clinvarRec.getPubmeds());
                varInfo.setClinvar(diseaseInfonfo);
                break;
            case "exac":
                var exacQuery = datastore.createQuery(ExacRecord.class);
                ExacRecord exacRec = exacQuery.field("_id").equal(new ObjectId(id)).find().next();
                for(ExacPopulation pop : ExacPopulation.values()){
                    PopulationData data = new PopulationData();
                    data.setAC(exacRec.getAlleleCounts().get(pop).get(hgvsIndex));
                    data.setAF(exacRec.getAlleleFrequencies().get(pop).get(hgvsIndex));
                    data.setAN(exacRec.getChromCounts().get(pop));
                    data.setHomAlt(exacRec.getAlleleHomCounts().get(pop).get(hgvsIndex));
                    if(varInfo.getPopulation().get(pop.name()) == null) {
                        PopulationInfo info = new PopulationInfo();
                        varInfo.getPopulation().put(pop.name(), info);
                    }
                    varInfo.getPopulation().get(pop.name()).setExac(data);
                }
                break;
            case "effect":
                var effectQuery = datastore.createQuery(VariantEffectRecord.class);
                VariantEffectRecord effectRec = effectQuery.field("_id").equal(new ObjectId(id)).find().next();
                EffectInfo effectInfo = new EffectInfo();
                effectInfo.setAnnotation(effectRec.getAnnotation().get(alt));
                effectInfo.setHgvsNomination(effectRec.getHgvsNomination().get(alt));
                varInfo.setEffect(effectInfo);
                varInfo.setId(effectRec.getRsId());
                break;

            case "g1k":
                var g1kQuery = datastore.createQuery(ThousandGenomesRecord.class);
                ThousandGenomesRecord g1kRec = g1kQuery.field("_id").equal(new ObjectId(id)).find().next();
                for(ThousandGenomesPopulation pop : ThousandGenomesPopulation.values()){
                    PopulationData data = new PopulationData();
                    data.setAC(g1kRec.getAlleleCounts().get(pop).get(hgvsIndex));
                    data.setAF(g1kRec.getAlleleFrequencies().get(pop).get(hgvsIndex));
                    data.setAN(g1kRec.getChromCounts().get(pop));
                    if(varInfo.getPopulation().get(pop.name()) == null) {
                        PopulationInfo info = new PopulationInfo();
                        varInfo.getPopulation().put(pop.name(), info);
                    }
                    varInfo.getPopulation().get(pop.name()).setThousandGenome(data);
                }
                break;
            case "dbnsfp":
                var dbnsfpQuery = datastore.createQuery(DBNSFPRecord.class);
                DBNSFPRecord dbnsfpRec = dbnsfpQuery.field("_id").equal(new ObjectId(id)).find().next();
                varInfo.setScores(getScoreInfo(hgvs, dbnsfpRec));
                break;
            case "acmg":
                var acmgQuery = datastore.createQuery(ACMGRecord.class);
                ACMGRecord acmgRecord = acmgQuery.field("_id").equal(new ObjectId(id)).find().next();
                varInfo.setAcmg(acmgRecord);
                break;
            case "gnomad_exome":
                var gnomadQuery = datastore.createQuery(GnomadExomeRecord.class);
                GnomadExomeRecord gnomadExomeRecord = gnomadQuery.field("_id").equal(new ObjectId(id)).find().next();
                for(GnomadExomePopulation pop : GnomadExomePopulation.values()){
                    PopulationData data = new PopulationData();
                    data.setAC(gnomadExomeRecord.getAlleleCounts().get(pop).get(hgvsIndex));
                    data.setAF(gnomadExomeRecord.getAlleleFrequencies().get(pop).get(hgvsIndex));
                    data.setAN(gnomadExomeRecord.getChromCounts().get(pop));
                    data.setHomAlt(gnomadExomeRecord.getAlleleHomCounts().get(pop).get(hgvsIndex));
                    if(varInfo.getPopulation().get(pop.name()) == null) {
                        PopulationInfo info = new PopulationInfo();
                        varInfo.getPopulation().put(pop.name(), info);
                    }
                    varInfo.getPopulation().get(pop.name()).setGnomadExome(data);
                }
                break;
        }

        if (varInfo.getRef() == null) {
            setRecord(varInfo, hit);
        }


        return varInfo;
    }

    private void setRecord(VariantInfo varInfo, SearchHit hit) {
        varInfo.setChrom((String) hit.getSourceAsMap().get("chrom"));
        varInfo.setPos((Integer) hit.getSourceAsMap().get("pos"));
        varInfo.setRef((String) hit.getSourceAsMap().get("ref"));
    }

    private ScoreInfo getScoreInfo(String hgvs, DBNSFPRecord record) {
        ScoreInfo scoreInfo = new ScoreInfo();
        int index = record.getHgvs().indexOf(hgvs);
        if (record.getSift() != null && record.getSift().size() > 0) {
            scoreInfo.setSift(new PredictedScore(record.getSift().get(index), record.getSiftPred().get(index)));
        }
        if (record.getLrt() != null && record.getLrt().size() > 0) {
            scoreInfo.setLrt(new PredictedScore(record.getLrt().get(index), record.getLrtPred().get(index)));
        }
        if (record.getPolyphen2() != null && record.getPolyphen2().size() > 0) {
            scoreInfo.setPolyphen2(new PredictedScore(record.getPolyphen2().get(index), record.getPolyphen2Pred().get(index)));
        }

        if (record.getCadd() != null && record.getCadd().size() > 0) {
            scoreInfo.setCadd(new PredictedScore(record.getCadd().get(index), null));
        }
        if (record.getDann() != null && record.getCadd().size() > 0) {
            scoreInfo.setDann(new PredictedScore(record.getDann().get(index), null));
        }
        if (record.getMutationTaster() != null && record.getMutationTaster().size() > 0) {
            scoreInfo.setMutationTaster(new PredictedScore(record.getMutationTaster().get(index), null));
        }
        if (record.getVest4() != null && record.getVest4().size() > 0) {
            scoreInfo.setVest4(new PredictedScore(record.getVest4().get(index), null));
        }

        return scoreInfo;
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

    private VariantInfo setVariantGene(VariantInfo variantInfo) throws IOException {
        RangeQueryBuilder startRangeQueryBuilder = QueryBuilders.rangeQuery("start");
        startRangeQueryBuilder.lte(variantInfo.getPos());
        RangeQueryBuilder endRangeQueryBuilder = QueryBuilders.rangeQuery("end");
        endRangeQueryBuilder.gte(variantInfo.getPos());
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("chrom", variantInfo.getChrom());
        SearchRequest searchRequest = new SearchRequest("genes");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        var boolQuery = QueryBuilders.boolQuery();
        boolQuery.must().add(startRangeQueryBuilder);
        boolQuery.must().add(endRangeQueryBuilder);
        boolQuery.must().add(matchQueryBuilder);
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        var searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        if (searchResponse.getHits().getTotalHits().value == 0) { //the variant is intergenic
            return variantInfo;
        }
        String symbol = (String) searchResponse.getHits().getAt(0).getSourceAsMap().get("symbol");
        variantInfo.setGene(symbol);

        return variantInfo;
    }
}
