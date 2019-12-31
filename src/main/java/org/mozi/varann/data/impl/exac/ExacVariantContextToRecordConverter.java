package org.mozi.varann.data.impl.exac;


import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.mozi.varann.data.impl.VariantContextToRecordConverter;
import org.mozi.varann.data.records.ExacRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link ExacRecord} objects
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class ExacVariantContextToRecordConverter implements VariantContextToRecordConverter<ExacRecord> {

	@Override
	public ExacRecord convert(VariantContext vc, ReferenceDictionary refDict) {
		ExacRecord builder = new ExacRecord();

		// Column-level properties from VCF file
		builder.setChrom(vc.getContig());
		builder.setPos(vc.getStart());
		String id = vc.getID();
		if(id != null && id.length() > 1) builder.setId(id);
		builder.setRef(vc.getReference().getBaseString());
		for (Allele all : vc.getAlternateAlleles()){
			builder.getAlt().add(all.getBaseString());
			GenomeVariant variant = new GenomeVariant(new GenomePosition(refDict, Strand.FWD, refDict.getContigNameToID().get(vc.getContig()), builder.getPos(), PositionType.ONE_BASED), builder.getRef(), all.getBaseString());
			builder.getHgvs().add(variant.toString());
		}

		builder.getFilter().addAll(vc.getFilters());

		// Fields from INFO VCF field

		// AN: Chromosome count
		int allAN = 0;
		for (ExacPopulation pop : ExacPopulation.values()) {
			if (pop == ExacPopulation.ALL)
				continue; // skip

			int an = vc.getAttributeAsInt("AN_" + pop, 0);
			builder.getChromCounts().put(pop, an);
			for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
				allAN += an;
		}
		builder.getChromCounts().put(ExacPopulation.ALL, allAN);

		// AC: Alternative allele count (+ het, hom,hemi)
		ArrayList<Integer> allAC = new ArrayList<>();
		ArrayList<Integer> allHet = new ArrayList<>();
		ArrayList<Integer> allHom = new ArrayList<>();
		ArrayList<Integer> allHemi = new ArrayList<>();
		for (int i = 0; i < vc.getAlternateAlleles().size(); ++i) {
			allAC.add(0);
			allHet.add(0);
			allHom.add(0);
			allHemi.add(0);
		}
		for (ExacPopulation pop : ExacPopulation.values()) {
			if (pop == ExacPopulation.ALL)
				continue; // skip

			// AC
			List<Integer> lst = vc.getAttributeAsList("AC_" + pop).stream().map(x -> Integer.parseInt((String) x))
				.collect(Collectors.toList());
			if (!lst.isEmpty()) {
				builder.getAlleleCounts().put(pop, lst);
				for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
					allAC.set(i, allAC.get(i) + lst.get(i));
			}

			// Het
			lst = vc.getAttributeAsList("Het_" + pop).stream().map(x -> Integer.parseInt((String) x))
				.collect(Collectors.toList());
			if (!lst.isEmpty()) {
				builder.getAlleleHetCounts().put(pop, lst);
				for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
					allHet.set(i, allHet.get(i) + lst.get(i));
			}

			// Hom
			lst = vc.getAttributeAsList("Hom_" + pop).stream().map(x -> Integer.parseInt((String) x))
				.collect(Collectors.toList());
			if (!lst.isEmpty()) {
				builder.getAlleleHomCounts().put(pop, lst);
				for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
					allHom.set(i, allHom.get(i) + lst.get(i));
			}

			// Hemi
			lst = vc.getAttributeAsList("Hemi_" + pop).stream().map(x -> Integer.parseInt((String) x))
				.collect(Collectors.toList());
			if (!lst.isEmpty()) {
				builder.getAlleleHemiCounts().put(pop, lst);
				for (int i = 0; i < vc.getAlternateAlleles().size(); ++i)
					allHemi.set(i, allHemi.get(i) + lst.get(i));
			}
		}
		builder.getAlleleCounts().put(ExacPopulation.ALL, allAC);
		if (!builder.getAlleleHetCounts().isEmpty())
			builder.getAlleleHetCounts().put(ExacPopulation.ALL, allHet);
		if (!builder.getAlleleHomCounts().isEmpty())
			builder.getAlleleHomCounts().put(ExacPopulation.ALL, allHom);
		if (!builder.getAlleleHemiCounts().isEmpty())
			builder.getAlleleHemiCounts().put(ExacPopulation.ALL, allHemi);

		return builder;
	}

}
