package org.mozi.varann.data.records;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.*;

@Entity("effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantEffectRecord {
    /**
     * Mongo Id
     */
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
     dbSNP rs id
     */
    @JsonIgnore
    public String rsId;

    /**
     * Annotation for the variant
     */
    private HashMap<String, Collection<AnnotationRecord>> annotation;

    /**
     * Hgvs string for each allele of the variant
     */
    @JsonIgnore
    private List<String> hgvs = new ArrayList<>() ;

    private HashMap<String, Collection<String>> hgvsNomination;

}
