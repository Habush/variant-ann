package org.mozi.varann.data.impl.annotation;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.*;

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
        builder.setAlt(records[4]);
        String hgvsRef = builder.getRef().equals("-") ? "" : builder.getRef();
        String hgvsAlt = builder.getAlt().equals("-") ? "" : builder.getAlt();

        GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(builder.getChrom()), (int) builder.getPos(), PositionType.ONE_BASED), hgvsRef, hgvsAlt);
        builder.setHgvs(variant.toString());

        //rsId;
        builder.setRsId(records[15]);
        builder.setGene(records[6]);
        //biotype
        builder.setBioType(records[5]);
        //Exonic Function
        builder.setExonicFunction(records[8]);
        if (!records[11].equals(".")) {
            List<String> ensGenes = Arrays.stream(records[11].split(",")).filter(e -> !e.equalsIgnoreCase("none")).collect(Collectors.toList());
            builder.setGeneId(ensGenes);
        }

        //aachanges
        if (!records[14].equals(".")) {
            builder.setEnsAAChange(parseAAChanges(records[14]));
        }
        if (!records[9].equals(".")) {
            builder.setRefAAChange(parseAAChanges(records[9]));
        }

        builder.setGnomeRecord(parseGnomadInfo(records));
        builder.setDbnsfpRecord(parseDBNSFPInfo(records));

        return builder;
    }

    private List<AminoAcidChange> parseAAChanges(String record) {
        List<AminoAcidChange> aaChanges = new ArrayList<>();

        String[] transcripts = record.split(",");
        for (String trans : transcripts) {
            String[] components = trans.split(":");
            if (components.length == 5) {
                aaChanges.add(new AminoAcidChange(components[0], components[1], components[2], components[3], components[4]));
            }
        }

        return aaChanges;
    }

    private GnomadGenomeRecord parseGnomadInfo(String[] records) {
        GnomadGenomeRecord genomeRecord = new GnomadGenomeRecord();

        if (!records[17].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.all, Double.parseDouble(records[17]));
        }
        if (!records[22].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.afr, Double.parseDouble(records[22]));
        }
        if (!records[23].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.sas, Double.parseDouble(records[23]));
        }
        if (!records[24].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.amr, Double.parseDouble(records[24]));
        }
        if (!records[25].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.eas, Double.parseDouble(records[25]));
        }
        if (!records[26].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.nfe, Double.parseDouble(records[26]));
        }
        if (!records[27].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.fin, Double.parseDouble(records[27]));
        }
        if (!records[28].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.asj, Double.parseDouble(records[28]));
        }
        if (!records[29].equals(".")) {
            genomeRecord.getAlleleFrequencies().put(GnomadExomePopulation.oth, Double.parseDouble(records[29]));
        }

        return genomeRecord;

    }

    private DBNSFPRecord parseDBNSFPInfo(String[] records) {
        DBNSFPRecord dbnsfpRecord = new DBNSFPRecord();
        if(!records[51].equals(".")){
            dbnsfpRecord.setSift(Double.parseDouble(records[51]));
            dbnsfpRecord.setSiftPred(records[53]);
        }
        if(!records[54].equals(".")){
            dbnsfpRecord.setLrt(Double.parseDouble(records[54]));
            dbnsfpRecord.setLrtPred(records[56]);
        }
        if(!records[57].equals(".")){
            dbnsfpRecord.setMutationTaster(Double.parseDouble(records[57]));
            dbnsfpRecord.setMutationTasterPred(records[59]);
        }
        if(!records[127].equals(".")){
            dbnsfpRecord.setPolyphen2(Double.parseDouble(records[127]));
        }
        if(!records[143].equals(".")){
            dbnsfpRecord.setCadd(Double.parseDouble(records[143]));
        }

        return dbnsfpRecord;
    }
}
