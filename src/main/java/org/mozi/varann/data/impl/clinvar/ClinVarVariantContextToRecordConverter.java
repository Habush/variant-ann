package org.mozi.varann.data.impl.clinvar;

import com.google.common.collect.*;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.*;

import java.util.*;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link ClinVarRecord} objects
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class ClinVarVariantContextToRecordConverter implements VariantContextToRecordConverter<ClinVarRecord> {

	private static <T> T getFromList(List<T> lst, int idx, T defaultValue) {
		if (idx >= lst.size())
			return defaultValue;
		else
			return lst.get(idx);
	}

	@Override
	public ClinVarRecord convert(VariantContext vc, ReferenceDictionary refDict) {
		ClinVarRecord builder = new ClinVarRecord();

		// Column-level properties from VCF file
		builder.setChrom(vc.getContig());
		builder.setPos(vc.getStart());
		builder.setAlleleId(vc.getAttributeAsString("ALLELEID", null));
		builder.setRef(vc.getReference().getBaseString());

		for (Allele all : vc.getAlternateAlleles()){
			builder.getAlt().add(all.getBaseString());
			GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), builder.getPos(), PositionType.ONE_BASED), builder.getRef(), all.getBaseString());
			builder.getHgvs().add(variant.toString());
		}

		// Fields from INFO VCF field

		int alleleSize = vc.getAlternateAlleles().size();

		// Create shortcuts to allele-wise lists of the INFO atributes we will try to interpret
		List<Object> hgvs = vc.getAttributeAsList("CLNHGVS");
		List<Object> clnOrigin = vc.getAttributeAsList("ORIGIN");

		List<Object> clnSrc = vc.getAttributeAsList("CLNVI");
		List<Object> clnSrcId = vc.getAttributeAsList("CLNSRCID");

		List<Object> clnSig = vc.getAttributeAsList("CLNSIG");
		List<Object> clnDiseaseDbId = vc.getAttributeAsList("CLNDISDB");
		List<Object> clnDiseaseDbName = vc.getAttributeAsList("CLNDN");
		List<Object> clnRevStat = vc.getAttributeAsList("CLNREVSTAT");
		Multimap<String, ClinVarAnnotation> annotationMap = ArrayListMultimap.create();
		for (int idx = 0; idx < alleleSize; ++idx) {
			// Construct annotation builder
			ClinVarAnnotation annoBuilder = new ClinVarAnnotation();
			// One element: CLNHGVS, CLNORIGIN
			if(hgvs.size() > 0){
				ArrayList<String> hgvsList = Lists.newArrayList(((String) hgvs.get(idx)).split("\\|"));
				// Set one-element lists into annoBuilder
				annoBuilder.setAlleleMapping(idx);
				if (hgvsList.size() != 1)
					throw new RuntimeException("Invalid HGVS size, must be 1");
				annoBuilder.setHgvsVariant(hgvsList.get(0));
			}

			if(clnOrigin.size() > 0) {
				ArrayList<String> clnOriginList  = Lists.newArrayList(((String) clnOrigin.get(idx)).split("\\|"));
				if (clnOriginList.size() != 1)
					throw new RuntimeException("Invalid CLNORIGIN size, must be 1");
				annoBuilder.setOrigin(ClinVarOrigin.fromInteger(Integer.parseInt(clnOriginList.get(0))));
			}

			// Variant source information: CLNSRC, CLNSRCID
			if(clnSrc.size() > 0 && clnSrcId.size() > 0){
				ArrayList<String> clnSrcList = Lists.newArrayList(((String) clnSrc.get(idx)).split("\\|"));
				ArrayList<String> clnSrcIdList = Lists.newArrayList(((String) clnSrcId.get(idx)).split("\\|"));
				// Construct variant source information
				List<ClinVarSourceInfo> sourceInfos = new ArrayList<>();
				if (clnSrcList.size() != clnSrcIdList.size())
					throw new RuntimeException("length of CLNSRC differ CLNSRCID");
				for (int i = 0; i < clnSrcList.size(); ++i)
					sourceInfos.add(new ClinVarSourceInfo(clnSrcList.get(i), clnSrcIdList.get(i)));
				annoBuilder.setSourceInfos(sourceInfos);
			}

			// Variant disease information: CLNSIG, CLNDSDB, CLINDSDBID, CLDSDBN, CLNREVSTAT, CLNACC
			if(clnSig.size() > 0 && clnDiseaseDbId.size() > 0 && clnDiseaseDbName.size() > 0 && clnRevStat.size() > 0){
				ArrayList<String> clnSigList = Lists.newArrayList(((String) clnSig.get(idx)).split("\\|"));
				ArrayList<String> clnDiseaseDbIdList = Lists.newArrayList(((String) clnDiseaseDbId.get(idx)).split("\\|"));
				ArrayList<String> clnDiseaseDbNameList = Lists
						.newArrayList(((String) clnDiseaseDbName.get(idx)).split("\\|"));
				ArrayList<String> clnRevStatList = Lists.newArrayList(((String) clnRevStat.get(idx)).split("\\|"));
				// Construct variant disease information
				List<ClinVarDiseaseInfo> diseaseInfos = new ArrayList<>();
				int numDiseaseAlleles = Collections
						.max(ImmutableList.of(clnSigList.size(), clnDiseaseDbIdList.size(),
								clnDiseaseDbNameList.size(), clnRevStatList.size()));
				for (int i = 0; i < numDiseaseAlleles; ++i)
					diseaseInfos.add(new ClinVarDiseaseInfo(
							clnSigList,
							getFromList(clnDiseaseDbIdList, i, ""),
							getFromList(clnDiseaseDbNameList, i, ""),
							clnRevStatList));
				annoBuilder.setDiseaseInfos(diseaseInfos);

			}

			annotationMap.put(vc.getAlternateAllele(idx).getBaseString(), annoBuilder);
		}

		builder.setAnnotations(Maps.newHashMap(annotationMap.asMap()));

		return builder;
	}

}
