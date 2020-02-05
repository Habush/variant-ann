package org.mozi.varann.data.impl.annotation;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.IntervarRecord;
import org.mozi.varann.data.records.VariantRecord;
import org.mozi.varann.data.records.AminoAcidChange;
import org.mozi.varann.data.records.OrphaDiseaseInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/13/20
 */
public class VariantRecordConverter implements TSVToRecordConverter<VariantRecord, String> {
    @Override
    public VariantRecord convert(String line, ReferenceDictionary refDict) {
        String[] records = line.split("\\t");
        VariantRecord builder = new VariantRecord();

        builder.setChrom(records[0]);
        builder.setPos(Integer.parseInt(records[1]));
        builder.setRef(records[3]);
        builder.getAlt().add(records[4]);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), (int) builder.getPos(), PositionType.ONE_BASED), builder.getRef(), records[4]);
        builder.getHgvs().add(variant.toString());

        //rsId;
        builder.setRsId(records[9]);
        builder.setGene(records[5]);
        //biotype
        builder.setBioType(records[6]);
        //Exonic Function
        builder.setExonicFunction(records[7]);
        if (!records[8].equals(".")) {
            List<String> ensGenes = Arrays.stream(records[8].split(",")).filter(e -> !e.equalsIgnoreCase("none")).collect(Collectors.toList());
            builder.setEnsGene(ensGenes);
        }

        //aachanges
        if (!records[10].equals(".")) {
            builder.setEnsAAChange(parseAAChanges(records[10]));
        }
        if (!records[11].equals(".")) {
            builder.setRefAAChange(parseAAChanges(records[11]));
        }

        IntervarRecord intervarBuilder = new IntervarRecord();
        intervarBuilder.setVerdict(records[13].trim());

        String[] evidences = records[14].split(";");

        for (String evidence : evidences) {
            String[] parts = evidence.split("=");
            String criteria = parts[0];
            String evi = parts[1];
            switch (criteria) {
                case "PVS1":
                    intervarBuilder.setPvs1(Integer.parseInt(evi));
                    break;
                case "PS":
                    intervarBuilder.setPs(convertToArray(evi));
                    break;
                case "PM":
                    intervarBuilder.setPm(convertToArray(evi));
                    break;
                case "PP":
                    intervarBuilder.setPp(convertToArray(evi));
                    break;
                case "BA1":
                    intervarBuilder.setBa1(Integer.parseInt(evi));
                    break;
                case "BS":
                    intervarBuilder.setBs(convertToArray(evi));
                    break;
                case "BP":
                    intervarBuilder.setBp(convertToArray(evi));
                    break;
                default:
                    throw new RuntimeException("Unknown criteria " + criteria);

            }
        }

        //Orpha info
        String[] orphaInfo = records[33].split("~");

        List<OrphaDiseaseInfo> orphaDiseaseInfos = new ArrayList<>();
        for (String info : orphaInfo) {
            String[] details = info.split("\\|");
            if (details.length >= 6) {
                OrphaDiseaseInfo diseaseInfo = new OrphaDiseaseInfo(details[0], details[1],
                        details[2], details[3], details[4], details[5].split(" "));
                orphaDiseaseInfos.add(diseaseInfo);
            }
        }

        intervarBuilder.setDiseaseInfos(orphaDiseaseInfos);

        builder.setIntervar(intervarBuilder);
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

    private List<AminoAcidChange> parseAAChanges(String record) {
        List<AminoAcidChange> aaChanges = new ArrayList<>();

        String[] transcripts = record.split(",");
        for (String trans : transcripts) {
            String[] components = trans.split(":");
            aaChanges.add(new AminoAcidChange(components[1], components[2], components[3], components[4]));
        }

        return aaChanges;
    }
}
