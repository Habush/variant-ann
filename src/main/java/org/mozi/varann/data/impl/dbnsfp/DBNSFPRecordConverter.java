package org.mozi.varann.data.impl.dbnsfp;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.apache.commons.csv.CSVRecord;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.DBNSFPRecord;
import org.mozi.varann.data.records.PredictedEffect;
import org.mozi.varann.data.records.PredictedScore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/23/19
 */
public class DBNSFPRecordConverter implements TSVToRecordConverter<DBNSFPRecord, CSVRecord> {

    private static final Pattern patScore = Pattern.compile("([\\.;]+)?([-|+]?\\d+\\.?\\d*$)");
    private static final Pattern patPred = Pattern.compile("([\\.;]+)?([T|D|B|N|P|U]$)");
    public DBNSFPRecord convert(CSVRecord record, ReferenceDictionary refDict)  {
        DBNSFPRecord builder = new DBNSFPRecord();

        builder.setChrom(record.get("#chr"));
        builder.setPos(Integer.parseInt(record.get("hg19_pos(1-based)")));
        builder.setRef(record.get("ref"));
        builder.getAlt().add(record.get("alt"));
        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), (int) builder.getPos(), PositionType.ONE_BASED), builder.getRef(), record.get("alt"));
        builder.getHgvs().add(variant.toString());

        //Add scores and predictions
        Matcher matScore = patScore.matcher(record.get("SIFT_score"));
        Matcher matPred = patPred.matcher(record.get("SIFT_pred"));
        if(matScore.matches() && matPred.matches()){
            builder.getSift().add(Double.parseDouble(matScore.group(2)));
            builder.getSiftPred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }
        matScore = patScore.matcher(record.get("Polyphen2_HDIV_score"));
        matPred = patPred.matcher(record.get("Polyphen2_HDIV_pred"));
        if(matScore.matches() && matPred.matches()){

            builder.getPolyphen2().add(Double.parseDouble(matScore.group(2)));
            builder.getPolyphen2Pred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }
        matScore = patScore.matcher(record.get("LRT_score"));
        matPred = patPred.matcher(record.get("LRT_pred"));
        if(matScore.matches() && matPred.matches()){
            builder.getLrt().add(Double.parseDouble(matScore.group(2)));
            builder.getLrtPred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }


        matScore = patScore.matcher(record.get("MutationTaster_score"));
        matPred = patPred.matcher(record.get("MutationAssessor_pred"));
        if(matScore.matches() && matPred.matches()){
            builder.getMutationTaster().add(Double.parseDouble(matScore.group(2)));
            builder.getMutationTasterPred().add(PredictedEffect.fromString(matPred.group(2)).name());
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
