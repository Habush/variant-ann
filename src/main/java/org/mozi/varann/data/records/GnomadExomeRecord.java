package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/22/20
 */
@EqualsAndHashCode(callSuper = true)
@Entity("gnomad_exome")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GnomadExomeRecord extends BaseRecord{
    @Id
    @JsonIgnore
    private ObjectId _id;

    @JsonIgnore
    private String id;

    //Entries of the INFO column

    /**
     * Observed alternative allele counts for each population
     */
    private HashMap<GnomadExomePopulation, List<Integer>> alleleCounts = new HashMap<>();

    /**
     * Observed alternative allele hom counts for each population
     */
    private HashMap<GnomadExomePopulation, List<Integer>> alleleHomCounts = new HashMap<>();

    /**
     * Chromosome counts for each population
     */
    private HashMap<GnomadExomePopulation,  Integer> chromCounts = new HashMap<>();
    /**
     * Observed alternative allele frequencies for each population
     */
    private HashMap<GnomadExomePopulation, List<Double>> alleleFrequencies = new HashMap<>();

}
