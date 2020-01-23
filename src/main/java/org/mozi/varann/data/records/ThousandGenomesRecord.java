package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

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

	private HashMap<ThousandGenomesPopulation, List<Integer>> alleleCounts = new HashMap<>();
	private HashMap<ThousandGenomesPopulation, Integer> chromCounts = new HashMap<>();
}
