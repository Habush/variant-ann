package org.mozi.varann.data.impl;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.mozi.varann.data.records.ACMGRecord;
import org.mozi.varann.data.records.OrphaDiseaseInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/13/20
 */
public class ACMGRecordConverter implements TSVToRecordConverter<ACMGRecord, String> {
    @Override
    public ACMGRecord convert(String line, ReferenceDictionary refDict) {
        String[] records = line.split("\\t");
        ACMGRecord builder = new ACMGRecord();

        builder.setChrom(records[0]);
        builder.setPos(Integer.parseInt(records[1]));
        builder.setRef(records[3]);
        builder.getAlt().add(records[4]);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), (int) builder.getPos(), PositionType.ONE_BASED), builder.getRef(), records[4]);
        builder.getHgvs().add(variant.toString());

        builder.setVerdict(records[13].trim());

        String[] evidences = records[14].split(";");

        for (String evidence : evidences) {
            String[] parts = evidence.split("=");
            String criteria = parts[0];
            String evi = parts[1];
            switch (criteria) {
                case "PVS1":
                    builder.setPvs1(Integer.parseInt(evi));
                    break;
                case "PS":
                    builder.setPs(convertToArray(evi));
                    break;
                case "PM":
                    builder.setPm(convertToArray(evi));
                    break;
                case "PP":
                    builder.setPp(convertToArray(evi));
                    break;
                case "BA1":
                    builder.setBa1(Integer.parseInt(evi));
                    break;
                case "BS":
                    builder.setBs(convertToArray(evi));
                    break;
                case "BP":
                    builder.setBp(convertToArray(evi));
                    break;
                default:
                    throw new RuntimeException("Unknown criteria " + criteria);

            }
        }

        //Exonic Function
        builder.setExonicFunction(records[7]);

        //Orpha info
        String[] orphaInfo = records[33].split("~");

        List<OrphaDiseaseInfo> orphaDiseaseInfos = new ArrayList<>();
        for(String info : orphaInfo) {
            String[] details = info.split("\\|");
            OrphaDiseaseInfo diseaseInfo = new OrphaDiseaseInfo(details[0], details[1],
                    details[2], details[3], details[4], details[5].split(" "));
            orphaDiseaseInfos.add(diseaseInfo);
        }

        builder.setDiseaseInfos(orphaDiseaseInfos);


        return builder;
    }

    private int[] convertToArray(String arr) {
        String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        int[] results = new int[items.length];

        for (int i = 0; i < items.length; i++) {
            results[i] = Integer.parseInt(items[i]);
        }
        return results;
    }
}
