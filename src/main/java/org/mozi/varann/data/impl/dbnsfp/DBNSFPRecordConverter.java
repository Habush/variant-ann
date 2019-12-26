package org.mozi.varann.data.impl.dbnsfp;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.apache.commons.csv.CSVRecord;
import org.mozi.varann.data.records.DBNSFPRecord;
import org.mozi.varann.data.records.PredictedEffect;
import org.mozi.varann.data.records.PredictedScore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/23/19
 */
public class DBNSFPRecordConverter {

    private static final Pattern patScore = Pattern.compile("([\\.;]+)?([-|+]?\\d+\\.?\\d*$)");
    private static final Pattern patPred = Pattern.compile("([\\.;]+)?([T|D|B|N|P|U]$)");
    public DBNSFPRecord convert(CSVRecord record, ReferenceDictionary refDict)  {
        DBNSFPRecord builder = new DBNSFPRecord();

        builder.setChrom(record.get("#chr"));
        builder.setPos(Integer.parseInt(record.get("hg19_pos(1-based)")));
        builder.setRef(record.get("ref"));
        builder.getAlt().add(record.get("alt"));
        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), builder.getPos()), builder.getRef(), record.get("alt"));
        builder.getHgvs().add(variant.toString());

        //Add scores and predictions
        Matcher matScore = patScore.matcher(record.get("SIFT_score"));
        Matcher matPred = patPred.matcher(record.get("SIFT_pred"));
        if(matScore.matches() && matPred.matches()){
            PredictedScore siftScore = new PredictedScore();
            siftScore.setScore(Double.parseDouble(matScore.group(2)));
            siftScore.setPrediction(PredictedEffect.fromString(matPred.group(2)).name());
            builder.getSift().add(siftScore);
        }
        matScore = patScore.matcher(record.get("Polyphen2_HDIV_score"));
        matPred = patPred.matcher(record.get("Polyphen2_HDIV_pred"));
        if(matScore.matches() && matPred.matches()){
            PredictedScore polyScore = new PredictedScore();
            polyScore.setScore(Double.parseDouble(matScore.group(2)));
            polyScore.setPrediction(PredictedEffect.fromString(matPred.group(2)).name());
            builder.getPolyphen2().add(polyScore);
        }
        matScore = patScore.matcher(record.get("LRT_score"));
        matPred = patPred.matcher(record.get("LRT_pred"));
        if(matScore.matches() && matPred.matches()){
            PredictedScore lrtScore = new PredictedScore();
            lrtScore.setScore(Double.parseDouble(matScore.group(2)));
            lrtScore.setPrediction(PredictedEffect.fromString(matPred.group(2)).name());
            builder.getLrt().add(lrtScore);
        }


        matScore = patScore.matcher(record.get("MutationTaster_score"));
        matPred = patPred.matcher(record.get("MutationAssessor_pred"));
        if(matScore.matches() && matPred.matches()){
            PredictedScore mutScore = new PredictedScore();
            mutScore.setScore(Double.parseDouble(matScore.group(2)));
            mutScore.setPrediction(PredictedEffect.fromString(matPred.group(2)).name());
            builder.getMutationTaster().add(mutScore);
        }


        matScore = patScore.matcher(record.get("VEST4_score"));
        if(matScore.matches()){
            builder.getVest4().add(Double.parseDouble(matScore.group(2)));
        }
        matScore = patScore.matcher(record.get("CADD_raw"));
        if(matScore.matches()){
            builder.getCadd().add(Double.parseDouble(matScore.group(2)));
        }
        matScore = patScore.matcher(record.get("DANN_score"));
        if(matScore.matches()){
            builder.getDann().add(Double.parseDouble(matScore.group(2)));
        }

        return builder;
    }

}
