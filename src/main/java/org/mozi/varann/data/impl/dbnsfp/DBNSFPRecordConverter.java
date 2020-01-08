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
public class DBNSFPRecordConverter implements TSVToRecordConverter<DBNSFPRecord, String> {

    private static final Pattern patScore = Pattern.compile("([\\.;]+)?([-|+]?\\d+\\.?\\d*$)");
    private static final Pattern patPred = Pattern.compile("([\\.;]+)?([T|D|B|N|P|U]$)");
    public DBNSFPRecord convert(String line, ReferenceDictionary refDict)  {
        String[] records = line.split("\\t");
        DBNSFPRecord builder = new DBNSFPRecord();

        builder.setChrom(records[0]);
        builder.setPos(Integer.parseInt(records[1]));
        builder.setRef(records[2]);
        builder.getAlt().add(records[3]);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), (int) builder.getPos(), PositionType.ONE_BASED), builder.getRef(), records[3]);
        builder.getHgvs().add(variant.toString());

        //Add scores and predictions
        Matcher matScore = patScore.matcher(records[30]);
        Matcher matPred = patPred.matcher(records[35]);
        if(matScore.matches() && matPred.matches()){
            builder.getSift().add(Double.parseDouble(matScore.group(2)));
            builder.getSiftPred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }
        matScore = patScore.matcher(records[36]);
        matPred = patPred.matcher(records[38]);
        if(matScore.matches() && matPred.matches()){

            builder.getPolyphen2().add(Double.parseDouble(matScore.group(2)));
            builder.getPolyphen2Pred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }
        matScore = patScore.matcher(records[42]);
        matPred = patPred.matcher(records[44]);
        if(matScore.matches() && matPred.matches()){
            builder.getLrt().add(Double.parseDouble(matScore.group(2)));
            builder.getLrtPred().add(PredictedEffect.fromString(matPred.group(2)).name());
        }


        matScore = patScore.matcher(records[46]);
        if(matScore.matches()){
            builder.getMutationTaster().add(Double.parseDouble(matScore.group(2)));
        }


        matScore = patScore.matcher(records[60]);
        if(matScore.matches()){
            builder.getVest4().add(Double.parseDouble(matScore.group(2)));
        }
        matScore = patScore.matcher(records[95]);
        if(matScore.matches()){
            builder.getCadd().add(Double.parseDouble(matScore.group(2)));
        }
        matScore = patScore.matcher(records[98]);
        if(matScore.matches()){
            builder.getDann().add(Double.parseDouble(matScore.group(2)));
        }

        return builder;
    }

}
