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
 * Represents on entry in the ExAC VCF database file
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@EqualsAndHashCode(callSuper = true)
@Entity("exac")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExacRecord extends BaseRecord {
	 @Id
	 @JsonIgnore
	 private ObjectId _id;

	 @JsonIgnore
	 private String id;
	// Fields up to the INFO column

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
}
