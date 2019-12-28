package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/27/19
 */
@Data
public class BaseRecord {
    /**
     * Name of the chromosome
     */
    @JsonIgnore
    public String chrom;
    /**
     * Position of the variant, 0-based
     */
    @JsonIgnore
    public int pos;
    /**
     * ID of the variant
     */
    @JsonIgnore
    public String alleleId;
    /**
     * Reference sequence
     */
    @JsonIgnore
    public String ref;
    /**
     * Alternative alleles in cluster
     */
    @JsonIgnore
    public List<String> alt = new ArrayList<>();

    /**
     * Hgvs strings for each alternate allele
     */
    @JsonIgnore
    public List<String> hgvs = new ArrayList<>();
}
