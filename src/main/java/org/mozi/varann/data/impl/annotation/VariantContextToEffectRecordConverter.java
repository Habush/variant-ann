package org.mozi.varann.data.impl.annotation;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.AnnotationRecord;
import org.mozi.varann.data.records.VariantEffectRecord;

import java.util.ArrayList;
import java.util.List;

public class VariantContextToEffectRecordConverter implements VariantContextToRecordConverter<VariantEffectRecord> {


    public VariantEffectRecord convert(VariantContext vc, ReferenceDictionary refDict) {

        VariantEffectRecord builder = new VariantEffectRecord();

        builder.setChrom(vc.getContig());
        builder.setPos(vc.getStart());
        builder.setId(vc.getID());
        builder.setRef(vc.getReference().getBaseString());
        for (Allele all : vc.getAlternateAlleles()) {
            builder.getAlt().add(all.getBaseString());
            GenomeVariant gv = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, Integer.parseInt(builder.getChrom()), builder.getPos()), builder.getRef(), all.getBaseString());
            builder.getHgvs().add(gv.toString());
        }


        builder.setRsId(vc.getAttributeAsString("RS", null));


        String annStr = vc.getAttributeAsString("ANN", null);
        List<String> annRecords = Lists.newArrayList(annStr.split(","));
        for(Allele all : vc.getAlternateAlleles()){
            List<AnnotationRecord> records = new ArrayList<>();
            for(String annRec : annRecords) {
                String[] recs = annRec.split("\\|");
                if(!recs[0].equals(all.getBaseString())) continue;
                AnnotationRecord record = new AnnotationRecord(recs[1], recs[2], recs[5], recs[6],
                        recs[7], recs[9], recs[10]);

                records.add(record);
            }
            builder.getAnnotation().put(all.getBaseString(), records);
        }

        //Build hgvs string for each allele

        return builder;
    }
}
