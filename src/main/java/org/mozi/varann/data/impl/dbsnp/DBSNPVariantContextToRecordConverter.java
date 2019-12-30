package org.mozi.varann.data.impl.dbsnp;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.DBSNPRecord;
import org.mozi.varann.data.records.GeneInfo;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Conversion of {@link VariantContext} to {@link DBSNPRecord} objects
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class DBSNPVariantContextToRecordConverter implements VariantContextToRecordConverter<DBSNPRecord> {

	/**
	 * Convert {@link VariantContext} to {@link DBSNPRecord}
	 *
	 * @param vc {@link VariantContext} to convert
	 * @return Resulting {@link DBSNPRecord}
	 */
	public DBSNPRecord convert(VariantContext vc, ReferenceDictionary refDict) {
		DBSNPRecord builder = new DBSNPRecord();

		// Column-level properties from VCF file
		builder.setChrom(vc.getContig());
		builder.setPos(vc.getStart());
		builder.setId(vc.getID());
		builder.setRef(vc.getReference().getBaseString());
		builder.getFilter().addAll(vc.getFilters());
		for (Allele all : vc.getAlternateAlleles()){
			builder.getAlt().add(all.getBaseString());
			GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), builder.getPos()), builder.getRef(), all.getBaseString());
			builder.getHgvs().add(variant.toString());
		}

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
