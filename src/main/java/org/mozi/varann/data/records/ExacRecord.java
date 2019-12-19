package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.mozi.varann.data.impl.exac.ExacPopulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents on entry in the ExAC VCF database file
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@Entity("exac")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExacRecord {
	 @Id
	 @JsonIgnore
	 private ObjectId _id;

	// Fields up to the INFO column

	/**
	 * Name of the chromosome
	 */
	@JsonIgnore
	private String chrom;
	/**
	 * Position of the variant, 0-based
	 */
	@JsonIgnore
	private int pos;
	/**
	 * ID of the variant
	 */
	@JsonIgnore
	private String id;
	/**
	 * Reference sequence
	 */
	@JsonIgnore
	private String ref;
	/**
	 * Alternative alleles in cluster
	 */
	@JsonIgnore
	private List<String> alt = new ArrayList<>();

	/**
	 * Hgvs string for each variant
	 */
	@JsonIgnore
	private List<String> hgvs = new ArrayList<>();
	/**
	 * Filters, NC: inconsistent genotype submission for at least one sample
	 */
	@JsonIgnore
	private List<String> filter = new ArrayList<>();

	// Entries of the INFO column

	/**
	 * Observed alternative allele counts for each population
	 */
	private HashMap<ExacPopulation, List<Integer>> alleleCounts = new HashMap<>();
	/**
	 * Observed alternative allele het counts for each population
	 */
	private HashMap<ExacPopulation, List<Integer>> alleleHetCounts = new HashMap<>();
	/**
	 * Observed alternative allele hom counts for each population
	 */
	private HashMap<ExacPopulation, List<Integer>> alleleHomCounts = new HashMap<>();
	/**
	 * Observed alternative allele hemi counts for each population
	 */
	private HashMap<ExacPopulation, List<Integer>> alleleHemiCounts = new HashMap<>();
	/**
	 * Chromsome counts for each population
	 */
	private HashMap<ExacPopulation, Integer> chromCounts = new HashMap<>();
	/**
	 * Observed alternative allele frequencies for each population
	 */
	private HashMap<ExacPopulation, List<Double>> alleleFrequencies = new HashMap<>();


	/**
	 * @return {@link ExacPopulation} with highest allele frequency for the given allele index (0 is first alternative
	 * allele)
	 */
	public ExacPopulation popWithHighestAlleleFreq(int alleleNo) {
		double bestFreq = -1;
		ExacPopulation bestPop = ExacPopulation.ALL;
		for (ExacPopulation pop : ExacPopulation.values()) {
			if (alleleNo < alleleFrequencies.get(pop).size())
				if (alleleFrequencies.get(pop).get(alleleNo) > bestFreq) {
					bestFreq = alleleFrequencies.get(pop).get(alleleNo);
					bestPop = pop;
				}
		}
		return bestPop;
	}

	/**
	 * @return Highest frequency of the given allele, 0 is first alternative allele
	 */
	public double highestAlleleFreq(int alleleNo) {
		return getAlleleFrequencies().get(popWithHighestAlleleFreq(alleleNo)).get(alleleNo);
	}

	public double highestAlleleFreqOverall() {
		double result = 0;
		for (int alleleNo = 0; alleleNo < alleleFrequencies.size(); ++alleleNo)
			if (alleleNo < getAlleleFrequencies().get(popWithHighestAlleleFreq(alleleNo)).size())
				result = Math.max(result, getAlleleFrequencies().get(popWithHighestAlleleFreq(alleleNo)).get(alleleNo));
		return result;
	}
}
