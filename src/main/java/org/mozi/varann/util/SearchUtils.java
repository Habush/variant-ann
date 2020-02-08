package org.mozi.varann.util;

import dev.morphia.Datastore;
import lombok.var;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.mozi.varann.data.records.*;
import org.mozi.varann.web.models.*;

import java.util.ArrayList;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/29/20
 */
public class SearchUtils {

    public static SearchRequest getRangeRequest(String[] indices, String contig, long start, long end) {
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

    public static SearchRequest getSearchRequest(String[] indices, String field, String value) {
        return getSearchRequest(indices, field, value, 0, 10);
    }

    public static SearchRequest getSearchRequest(String[] indices, String field, String value, int from, int size) {
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

    @SuppressWarnings("unchecked")
    public static VariantInfo buildVariantInfo(Datastore datastore ,SearchHits hits, String hgvs) {
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
                case "variant":
                    var varQuery = datastore.createQuery(VariantRecord.class);
                    VariantRecord record = varQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setGene(record.getGene());
                    varInfo.setBioType(record.getBioType());
                    varInfo.setExonicFunction(record.getExonicFunction());
                    varInfo.setEnsembleGenes(record.getEnsGene());
                    varInfo.setEnsembleTranscripts(record.getEnsAAChange());
                    varInfo.setRefSeqTranscripts(record.getRefAAChange());
                    varInfo.setAcmg(record.getIntervar());
                    varInfo.setId(record.getRsId());
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
    public static VariantInfo buildVariantInfo(Datastore datastore, SearchHit hit, String hgvs) {
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
                    data.setAC(exacRec.getAlleleCounts().containsKey(pop) ? exacRec.getAlleleCounts().get(pop).get(hgvsIndex) : -1);
                    data.setAF(exacRec.getAlleleFrequencies().containsKey(pop) ? exacRec.getAlleleFrequencies().get(pop).get(hgvsIndex) : -1);
                    data.setAN(exacRec.getChromCounts().getOrDefault(pop, -1));
                    data.setHomAlt(exacRec.getAlleleHomCounts().get(pop).get(hgvsIndex));
                    if(varInfo.getPopulation().get(pop.name()) == null) {
                        PopulationInfo info = new PopulationInfo();
                        varInfo.getPopulation().put(pop.name(), info);
                    }
                    varInfo.getPopulation().get(pop.name()).setExac(data);
                }
                break;
            case "variant":
                var varQuery = datastore.createQuery(VariantRecord.class);
                VariantRecord record = varQuery.field("_id").equal(new ObjectId(id)).find().next();
                varInfo.setGene(record.getGene());
                varInfo.setBioType(record.getBioType());
                varInfo.setExonicFunction(record.getExonicFunction());
                varInfo.setEnsembleGenes(record.getEnsGene());
                varInfo.setEnsembleTranscripts(record.getEnsAAChange());
                varInfo.setRefSeqTranscripts(record.getRefAAChange());
                varInfo.setAcmg(record.getIntervar());
                varInfo.setId(record.getRsId());
                break;

            case "g1k":
                var g1kQuery = datastore.createQuery(ThousandGenomesRecord.class);
                ThousandGenomesRecord g1kRec = g1kQuery.field("_id").equal(new ObjectId(id)).find().next();
                for(ThousandGenomesPopulation pop : ThousandGenomesPopulation.values()){
                    PopulationData data = new PopulationData();
                    data.setAC(g1kRec.getAlleleCounts().containsKey(pop) ? g1kRec.getAlleleCounts().get(pop).get(hgvsIndex) : -1);
                    data.setAF(g1kRec.getAlleleFrequencies().containsKey(pop) ? g1kRec.getAlleleFrequencies().get(pop).get(hgvsIndex) : -1);
                    data.setAN( g1kRec.getChromCounts().getOrDefault(pop, -1));
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
            case "gnomad_exome":
                var gnomadQuery = datastore.createQuery(GnomadExomeRecord.class);
                GnomadExomeRecord gnomadExomeRecord = gnomadQuery.field("_id").equal(new ObjectId(id)).find().next();
                for(GnomadExomePopulation pop : GnomadExomePopulation.values()){
                    PopulationData data = new PopulationData();
                    data.setAC(gnomadExomeRecord.getAlleleCounts().containsKey(pop) ? gnomadExomeRecord.getAlleleCounts().get(pop).get(hgvsIndex) : -1);
                    data.setAF(gnomadExomeRecord.getAlleleFrequencies().containsKey(pop) ? gnomadExomeRecord.getAlleleFrequencies().get(pop).get(hgvsIndex) : -1);
                    data.setAN(gnomadExomeRecord.getChromCounts().getOrDefault(pop, -1));
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

    public static void setRecord(VariantInfo varInfo, SearchHit hit) {
        varInfo.setChrom((String) hit.getSourceAsMap().get("chrom"));
        varInfo.setPos((Integer) hit.getSourceAsMap().get("pos"));
        varInfo.setRef((String) hit.getSourceAsMap().get("ref"));
    }

    public static ScoreInfo getScoreInfo(String hgvs, DBNSFPRecord record) {
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
}
