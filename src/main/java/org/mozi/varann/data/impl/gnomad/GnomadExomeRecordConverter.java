package org.mozi.varann.data.impl.gnomad;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.ExacPopulation;
import org.mozi.varann.data.records.GnomadExomePopulation;
import org.mozi.varann.data.records.GnomadExomeRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/22/20
 */
public class GnomadExomeRecordConverter  implements VariantContextToRecordConverter<GnomadExomeRecord> {
    @Override
    public GnomadExomeRecord convert(VariantContext vc, ReferenceDictionary refDict) {
        GnomadExomeRecord builder = new GnomadExomeRecord();

        builder.setChrom(vc.getContig());
        builder.setPos(vc.getStart());
        String id = vc.getID();
        if(id != null && id.length() > 1) builder.setId(id);
        builder.setRef(vc.getReference().getBaseString());
        for (Allele all : vc.getAlternateAlleles()){
            builder.getAlt().add(all.getBaseString());
            GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), (int)builder.getPos(), PositionType.ONE_BASED), builder.getRef(), all.getBaseString());
            builder.getHgvs().add(variant.toString());
        }


        int allAN = 0;

        // AC: Alternative allele count (+ het, hom,hemi)
        ArrayList<Integer> allAC = new ArrayList<>();
        ArrayList<Integer> allHom = new ArrayList<>();
        ArrayList<Double> allAF = new ArrayList<>();
        for (int i = 0; i < vc.getAlternateAlleles().size(); ++i) {
            allAC.add(0);
            allHom.add(0);
            allAF.add(0.0);
        }
        for (GnomadExomePopulation pop : GnomadExomePopulation.values()) {
            if (pop == GnomadExomePopulation.all)
                continue; // skip
            //AN
            int an = vc.getAttributeAsInt("AN_" + pop, 0);
            builder.getChromCounts().put(pop, an);
            for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
                allAN += an;
            // AC
            List<Integer> lst = vc.getAttributeAsList("AC_" + pop).stream().map(x -> Integer.parseInt((String) x))
                    .collect(Collectors.toList());
            if (!lst.isEmpty()) {
                builder.getAlleleCounts().put(pop, lst);
                for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
                    allAC.set(i, allAC.get(i) + lst.get(i));
            }


            // Hom
            lst = vc.getAttributeAsList("nhomalt_" + pop).stream().map(x -> Integer.parseInt((String) x))
                    .collect(Collectors.toList());
            if (!lst.isEmpty()) {
                builder.getAlleleHomCounts().put(pop, lst);
                for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
                    allHom.set(i, allHom.get(i) + lst.get(i));
            }

            //AF: alternate allele frequencies
            List<Double> dlst = vc.getAttributeAsList("AF_" + pop).stream().map(x -> Double.parseDouble((String) x)).collect(Collectors.toList());
            if(!dlst.isEmpty()){
                builder.getAlleleFrequencies().put(pop, dlst);
                for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
                    allAF.set(i, allAF.get(i) + dlst.get(i));
            }
        }
        builder.getChromCounts().put(GnomadExomePopulation.all, allAN);
        builder.getAlleleCounts().put(GnomadExomePopulation.all, allAC);
        builder.getAlleleFrequencies().put(GnomadExomePopulation.all, allAF);
        if (!builder.getAlleleHomCounts().isEmpty())
            builder.getAlleleHomCounts().put(GnomadExomePopulation.all, allHom);

        return builder;
    }
}
