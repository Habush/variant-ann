package org.mozi.varann.data.impl.annotation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.AnnotationRecord;
import org.mozi.varann.data.records.VariantEffectRecord;

import java.util.List;
import java.util.stream.Collectors;

public class VariantContextToEffectRecordConverter implements VariantContextToRecordConverter<VariantEffectRecord> {


    public VariantEffectRecord convert(VariantContext vc, ReferenceDictionary refDict) {

        VariantEffectRecord builder = new VariantEffectRecord();

        builder.setChrom(vc.getContig());
        builder.setPos(vc.getStart());
        builder.setRef(vc.getReference().getBaseString());
        for (Allele all : vc.getAlternateAlleles()) {
            builder.getAlt().add(all.getBaseString());
            GenomeVariant gv = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), builder.getPos(), PositionType.ONE_BASED), builder.getRef(), all.getBaseString());
            builder.getHgvs().add(gv.toString());
        }


        builder.setRsId(vc.getAttributeAsString("RS", null));


        String annStr = vc.getAttributeAsString("ANN", null);
        if(annStr == null) {
            return null;
        }
        annStr = annStr.replace("[", "").replace("]", "");
        Multimap<String, String> hgvsMap = ArrayListMultimap.create();
        Multimap<String, AnnotationRecord> annotationRecMap = ArrayListMultimap.create();
        List<String> annRecords = Lists.newArrayList(annStr.split(","));
        for (String annRec : annRecords) {
            String[] recs = annRec.split("\\|");
            String alt = recs[0];

            String nomination = "";
            AnnotationRecord record = null;
            if(recs.length < 10 ){
                String geneSymbol = recs[3],
                        featureType = recs[5],
                        featureId = recs[6],
                        bioType = recs[7];
                nomination = String.format("%s(%s):n.%d%s>%s", featureId, geneSymbol, builder.getPos(), builder.getRef(), alt);
                record = new AnnotationRecord(recs[1], recs[2], featureType, featureId, bioType, "", "");
            } else {
                String geneSymbol = recs[3],
                        featureType = recs[5],
                        featureId = recs[6],
                        bioType = recs[7],
                        cdsChange = recs[9],
                        proteinChange = recs[10];
                 record = new AnnotationRecord(recs[1], recs[2], featureType, featureId, bioType, cdsChange, proteinChange);
                if (!cdsChange.isEmpty() & bioType.equals("Coding")) {
                    nomination = String.format("%s(%s):%s(%s)", featureId, geneSymbol, cdsChange, proteinChange);
                } else if (!cdsChange.isEmpty() && bioType.equals("Noncoding")) {
                    nomination = String.format("%s(%s):%s", featureId, geneSymbol, cdsChange);
                } else {
                    nomination = String.format("%s(%s):n.%d%s>%s", featureId, geneSymbol, builder.getPos(), builder.getRef(), alt);
                }
            }
            hgvsMap.put(alt, nomination);
            annotationRecMap.put(alt, record);
        }

        builder.setAnnotation(Maps.newHashMap(annotationRecMap.asMap()));
        builder.setHgvsNomination(Maps.newHashMap(hgvsMap.asMap()));

        //DBSNP
        builder.getFilter().addAll(vc.getFilters());

        // Fields from INFO VCF field
        builder.setRsId(vc.getAttributeAsString("RS", null));
        builder.setRsPos(vc.getAttributeAsInt("RSPOS", -1));
        builder.setReversed(vc.hasAttribute("RV"));
        builder.setVariantProperty(null); // TODO
        builder.setDbSNPBuildID(vc.getAttributeAsInt("dbSNPBuildID", -1));

        // TODO: can be cleaned up by having methods in Enum
        switch (vc.getAttributeAsInt("SAO", 0)) {
            case 0:
                builder.setOrigin("UNSPECIFIED");
                break;
            case 1:
                builder.setOrigin("GERMLINE");
                break;
            case 2:
                builder.setOrigin("SOMATIC");
                break;
            case 3:
                builder.setOrigin("BOTH");
                break;
        }

        // TODO: can be cleaned up by having methods in Enum
        int suspectCode = vc.getAttributeAsInt("SSR", 0);
        if (suspectCode == 0) {
            builder.getVariantSuspectReasonCode().add("UNSPECIFIED");
        } else {
            if ((suspectCode & 1) == 1)
                builder.getVariantSuspectReasonCode().add("PARALOG");
            if ((suspectCode & 2) == 2)
                builder.getVariantSuspectReasonCode().add("BY_EST");
            if ((suspectCode & 4) == 4)
                builder.getVariantSuspectReasonCode().add("OLD_ALIGN");
            if ((suspectCode & 8) == 8)
                builder.getVariantSuspectReasonCode().add("PARA_EST");
            if ((suspectCode & 16) == 16)
                builder.getVariantSuspectReasonCode().add("G1K_FAILED");
            if ((suspectCode & 1024) == 1024)
                builder.getVariantSuspectReasonCode().add("OTHER");
        }

        builder.setWeights(vc.getAttributeAsInt("WGT", 0));
        builder.setVariationClass(vc.getAttributeAsString("VC", null));

        builder.setPrecious(vc.hasAttribute("PM"));
        builder.setThirdPartyAnnotation(vc.hasAttribute("TPA"));
        builder.setPubMedCentral(vc.hasAttribute("PMC"));
        builder.setThreeDStructure(vc.hasAttribute("S3D"));
        builder.setSubmitterLinkOut(vc.hasAttribute("SLO"));
        builder.setNonSynonymousFrameShift(vc.hasAttribute("NSF"));
        builder.setNonSynonymousMissense(vc.hasAttribute("NSM"));
        builder.setNonSynonymousNonsense(vc.hasAttribute("NSN"));
        builder.setReference(vc.hasAttribute("REF"));
        builder.setInThreePrimeUTR(vc.hasAttribute("U3"));
        builder.setInFivePrimeUTR(vc.hasAttribute("U5"));
        builder.setInAcceptor(vc.hasAttribute("ASS"));
        builder.setInDonor(vc.hasAttribute("DSS"));
        builder.setInIntron(vc.hasAttribute("INT"));
        builder.setInThreePrime(vc.hasAttribute("R3"));
        builder.setInFivePrime(vc.hasAttribute("R5"));
        builder.setOtherVariant(vc.hasAttribute("OTH"));
        builder.setAssemblySpecific(vc.hasAttribute("ASP"));
        builder.setAssemblyConflict(vc.hasAttribute("CFL"));
        builder.setMutation(vc.hasAttribute("MUT"));
        builder.setValidated(vc.hasAttribute("VLD"));
        builder.setFivePercentAll(vc.hasAttribute("G5A"));
        builder.setFivePercentOne(vc.hasAttribute("G5"));
        builder.setGenotypesAvailable(vc.hasAttribute("GNO"));
        builder.setG1kPhase1(vc.hasAttribute("KGPhase1"));
        builder.setG1kPhase3(vc.hasAttribute("GKPhase3"));
        builder.setClinicalDiagnosticAssay(vc.hasAttribute("CDA"));
        builder.setLocusSpecificDatabase(vc.hasAttribute("LSD"));
        builder.setMicroattributionThirdParty(vc.hasAttribute("MTP"));
        builder.setHasOMIMOrOMIA(vc.hasAttribute("OM"));
        builder.setContigAlelleNotVariant(vc.hasAttribute("NOC"));
        builder.setWithdrawn(vc.hasAttribute("WTD"));
        builder.setNonOverlappingAlleleSet(vc.hasAttribute("NOV"));
        builder.getAlleleFrequenciesG1K().addAll(vc.getAttributeAsList("CAF").stream().map(x -> {
            if (".".equals(x))
                return 0.0;
            else
                return (Double) Double.parseDouble((String) x);
        }).collect(Collectors.toList()));
        if (!builder.getAlleleFrequenciesG1K().isEmpty())
            builder.getAlleleFrequenciesG1K().subList(0, 1).clear();
        builder.setCommon(vc.hasAttribute("COMMON"));
        builder.getOldVariants().addAll(
                vc.getAttributeAsList("OLD_VARIANT").stream().map(x -> (String) x).collect(Collectors.toList()));

        return builder;
    }
}
