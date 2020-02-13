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

    public static SearchRequest getSearchRequest(String[] indices, String[] fields, String[] value)  {
        if(fields.length != value.length) {
            throw new IllegalArgumentException("fields and values size must be equal");
        }
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        var boolQuery = QueryBuilders.boolQuery();
        for(int i = 0; i < fields.length; i++){
            boolQuery.must(QueryBuilders.matchQuery(fields[i], value[i]));
        }

        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    @SuppressWarnings("unchecked")
    public static VariantInfo buildVariantInfo(Datastore datastore ,SearchHits hits, String hgvs) {
        VariantInfo varInfo = new VariantInfo();
        for (var hit : hits) {
            String index = hit.getIndex();
            String id = hit.getId();

            int hgvsIndex = 0;
            if( hit.getSourceAsMap().get("hgvs") instanceof ArrayList){
                hgvsIndex = ((ArrayList<String>) hit.getSourceAsMap().get("hgvs")).indexOf(hgvs);
                String alt = ((ArrayList<String>) hit.getSourceAsMap().get("alt")).get(hgvsIndex);
                if (varInfo.getAlt() == null) {
                    varInfo.setAlt(alt);
                    varInfo.setHgvs(hgvs);
                }
            } else {
                varInfo.setHgvs(hgvs);
                varInfo.setAlt((String) hit.getSourceAsMap().get("alt"));
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
                    varInfo.setEnsembleGenes(record.getGeneId());
                    varInfo.setEnsembleTranscripts(record.getEnsAAChange());
                    varInfo.setRefSeqTranscripts(record.getRefAAChange());
                    varInfo.setId(record.getRsId());

                    GnomadGenomeRecord gnomadGenomeRecord = record.getGnomeRecord();
                    if(gnomadGenomeRecord != null) {
                        for (GnomadExomePopulation pop : GnomadExomePopulation.values()) {
                            PopulationData data = new PopulationData();
                            data.setAF(gnomadGenomeRecord.getAlleleFrequencies().containsKey(pop) ? gnomadGenomeRecord.getAlleleFrequencies().get(pop) : -1);
                            if (varInfo.getPopulation().get(pop.name()) == null) {
                                PopulationInfo info = new PopulationInfo();
                                varInfo.getPopulation().put(pop.name(), info);
                            }
                            varInfo.getPopulation().get(pop.name()).setGnomadGenome(data);
                        }
                    }

                    varInfo.setScores(getScoreInfo(record.getDbnsfpRecord()));

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
                case "intervar":
                    var intervarQuery = datastore.createQuery(IntervarRecord.class);
                    IntervarRecord intervarRecord = intervarQuery.field("_id").equal(new ObjectId(id)).find().next();
                    varInfo.setAcmg(intervarRecord);
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
        int hgvsIndex = 0;
        if( hit.getSourceAsMap().get("hgvs") instanceof ArrayList){
            hgvsIndex = ((ArrayList<String>) hit.getSourceAsMap().get("hgvs")).indexOf(hgvs);
            String alt = ((ArrayList<String>) hit.getSourceAsMap().get("alt")).get(hgvsIndex);
            if (varInfo.getAlt() == null) {
                varInfo.setAlt(alt);
                varInfo.setHgvs(hgvs);
            }
        } else {
            varInfo.setHgvs(hgvs);
            varInfo.setAlt((String) hit.getSourceAsMap().get("alt"));
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
                varInfo.setEnsembleGenes(record.getGeneId());
                varInfo.setEnsembleTranscripts(record.getEnsAAChange());
                varInfo.setRefSeqTranscripts(record.getRefAAChange());
                varInfo.setId(record.getRsId());

                GnomadGenomeRecord gnomadGenomeRecord = record.getGnomeRecord();
                if(gnomadGenomeRecord != null){
                    for(GnomadExomePopulation pop : GnomadExomePopulation.values()){
                        PopulationData data = new PopulationData();
                        data.setAF(gnomadGenomeRecord.getAlleleFrequencies().containsKey(pop) ? gnomadGenomeRecord.getAlleleFrequencies().get(pop):-1);
                        if(varInfo.getPopulation().get(pop.name()) == null) {
                            PopulationInfo info = new PopulationInfo();
                            varInfo.getPopulation().put(pop.name(), info);
                        }
                        varInfo.getPopulation().get(pop.name()).setGnomadGenome(data);
                    }
                }

                varInfo.setScores(getScoreInfo(record.getDbnsfpRecord()));

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

            case "intervar":
                var intervarQuery = datastore.createQuery(IntervarRecord.class);
                IntervarRecord intervarRecord = intervarQuery.field("_id").equal(new ObjectId(id)).find().next();
                varInfo.setAcmg(intervarRecord);
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

    public static ScoreInfo getScoreInfo(DBNSFPRecord record) {
        if(record != null){
            ScoreInfo scoreInfo = new ScoreInfo();
            scoreInfo.setCadd(new PredictedScore(record.getCadd(), null));
            scoreInfo.setMutationTaster(new PredictedScore(record.getMutationTaster(), record.getMutationTasterPred()));
            scoreInfo.setPolyphen2(new PredictedScore(record.getPolyphen2(), null));
            scoreInfo.setLrt(new PredictedScore(record.getLrt(), record.getLrtPred()));
            scoreInfo.setSift(new PredictedScore(record.getSift(), record.getSiftPred() ));

            return scoreInfo;
        }

        return null;

    }
}
