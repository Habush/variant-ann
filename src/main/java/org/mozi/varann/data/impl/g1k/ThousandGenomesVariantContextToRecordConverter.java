package org.mozi.varann.data.impl.g1k;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.GnomadExomePopulation;
import org.mozi.varann.data.records.ThousandGenomesPopulation;
import org.mozi.varann.data.records.ThousandGenomesRecord;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link ThousandGenomesRecord} objects
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class ThousandGenomesVariantContextToRecordConverter implements VariantContextToRecordConverter<ThousandGenomesRecord> {

	@Override
	public ThousandGenomesRecord convert(VariantContext vc, ReferenceDictionary refDict) {
		ThousandGenomesRecord builder = new ThousandGenomesRecord();

		// Column-level properties from VCF file
		builder.setChrom(vc.getContig());
		builder.setPos(vc.getStart());
		builder.setRsId(vc.getID());
		builder.setRef(vc.getReference().getBaseString());
		for (Allele all : vc.getAlternateAlleles()){
			builder.getAlt().add(all.getBaseString());
			GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), (int)builder.getPos(), PositionType.ONE_BASED), builder.getRef(), all.getBaseString());
			builder.getHgvs().add(variant.toString());
		}

		builder.getFilter().addAll(vc.getFilters());
		// Fields from INFO VCF field


		double afrFreq = vc.getAttributeAsDouble("AFR_AF", 0);
		double amrFreq = vc.getAttributeAsDouble("AMR_AF", 0);
		double asnFreq = vc.getAttributeAsDouble("ASN_AF", 0);
		double eurFreq = vc.getAttributeAsDouble("EUR_AF", 0);
		double allFreq = vc.getAttributeAsDouble("AF", 0);

		builder.getAlleleFrequencies().put(ThousandGenomesPopulation.afr, Lists.newArrayList(afrFreq));
		builder.getAlleleFrequencies().put(ThousandGenomesPopulation.amr, Lists.newArrayList(amrFreq));
		builder.getAlleleFrequencies().put(ThousandGenomesPopulation.asn, Lists.newArrayList(asnFreq));
		builder.getAlleleFrequencies().put(ThousandGenomesPopulation.eur, Lists.newArrayList(eurFreq));
		builder.getAlleleFrequencies().put(ThousandGenomesPopulation.all, Lists.newArrayList(allFreq));


		int alleleCount = vc.getAttributeAsInt("AC", 0);
		int chromCount = vc.getAttributeAsInt("AN", 0);

		builder.getAlleleCounts().put(ThousandGenomesPopulation.all, Lists.newArrayList(alleleCount));
		builder.getChromCounts().put(ThousandGenomesPopulation.all, chromCount);

		return builder;
	}

}
