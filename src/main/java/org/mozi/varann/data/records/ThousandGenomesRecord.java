package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.mozi.varann.data.impl.g1k.ThousandGenomesPopulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents on entry in the thousand genomes VCF database file
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity("g1k")
@AllArgsConstructor
@NoArgsConstructor
public class ThousandGenomesRecord extends BaseRecord {
	@Id
	@JsonIgnore
	private ObjectId _id;
	@JsonIgnore
	private String rsId;
	// Fields up to the INFO column
	/**
	 * Filters, RF: failed random forest, AC0: Allele count is zero, InbreedingCoeff: InbreedingCoeff < threshold, LCR:
	 * in a low-complexity region, SEGDUP: in a segmental duplication region
	 */@JsonIgnore
	private List<String> filter = new ArrayList<>();

	// Entries of the INFO column
	/**
	 * Observed alternative allele frequencies for each population
	 */
	private HashMap<ThousandGenomesPopulation, List<Double>> alleleFrequencies = new HashMap<>();


	/**
	 * @return {@link ThousandGenomesPopulation} with highest allele frequency for the given allele index (0 is first alternative
	 * allele)
	 */
	public ThousandGenomesPopulation popWithHighestAlleleFreq(int alleleNo) {
		double bestFreq = -1;
		ThousandGenomesPopulation bestPop = ThousandGenomesPopulation.ALL;
		for (ThousandGenomesPopulation pop : ThousandGenomesPopulation.values()) {
			if (alleleFrequencies.get(pop) != null && alleleNo < alleleFrequencies.get(pop).size()
				&& alleleFrequencies.get(pop).get(alleleNo) > bestFreq) {
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

	/**
	 * @return Highest frequency of any allele in any population
	 */
	public double highestAlleleFreqOverall() {
		double maxAlleleFreq = 0;
		for (int alleleNo = 0; alleleNo < alt.size(); ++alleleNo)
			maxAlleleFreq = Math.max(maxAlleleFreq,
					getAlleleFrequencies().get(popWithHighestAlleleFreq(alleleNo)).get(alleleNo));
		return maxAlleleFreq;
	}
}
