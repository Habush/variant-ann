package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/12/20
 */
@Data
@Embedded
@AllArgsConstructor
@NoArgsConstructor
public class GnomadGenomeRecord {

    /**
     * Observed alternative allele frequencies for each population
     */
    private HashMap<GnomadExomePopulation, Double> alleleFrequencies = new HashMap<>();
}
