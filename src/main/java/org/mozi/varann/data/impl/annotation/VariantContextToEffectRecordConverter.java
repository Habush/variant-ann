package org.mozi.varann.data.impl.annotation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.AnnotationRecord;
import org.mozi.varann.data.records.VariantEffectRecord;

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
            GenomeVariant gv = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), builder.getPos()), builder.getRef(), all.getBaseString());
            builder.getHgvs().add(gv.toString());
        }


        builder.setRsId(vc.getAttributeAsString("RS", null));


        String annStr = vc.getAttributeAsString("ANN", null).replace("[", "").replace("]", "");
        Multimap<String, String> hgvsMap = ArrayListMultimap.create();
        Multimap<String, AnnotationRecord> annotationRecMap = ArrayListMultimap.create();
        List<String> annRecords = Lists.newArrayList(annStr.split(","));
        for (String annRec : annRecords) {
            String[] recs = annRec.split("\\|");
            String alt = recs[0];

            String geneSymbol = recs[3],
                    featureType = recs[5],
                    featureId = recs[6],
                    bioType = recs[7],
                    cdsChange = recs[9],
                    proteinChange = recs[10];
            AnnotationRecord record = new AnnotationRecord(recs[1], recs[2], featureType, featureId, bioType, cdsChange, proteinChange);
            String nomination = "";
            if (!cdsChange.isEmpty() & bioType.equals("Coding")) {
                nomination = String.format("%s(%s):%s(%s)", featureId, geneSymbol, cdsChange, proteinChange);
            } else if (!cdsChange.isEmpty() && bioType.equals("Noncoding")) {
                nomination = String.format("%s(%s):%s", featureId, geneSymbol, cdsChange);
            } else {
                nomination = String.format("%s(%s):n.%d%s>%s", featureId, geneSymbol, builder.getPos(), builder.getRef(), alt);
            }
            hgvsMap.put(alt, nomination);
            annotationRecMap.put(alt, record);
        }

        builder.setAnnotation(Maps.newHashMap(annotationRecMap.asMap()));
        builder.setHgvsNomination(Maps.newHashMap(hgvsMap.asMap()));
        return builder;
    }
}
