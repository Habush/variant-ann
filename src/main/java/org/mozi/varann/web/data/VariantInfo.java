package org.mozi.varann.web.data;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import lombok.Data;
import org.mozi.varann.data.records.*;

import java.util.HashMap;

/**
 * An aggregator class that holds the info about a variant from all dbs
 */
@Data
public class VariantInfo {

    /**
     * Name of the chromosome
     */
    private String chrom;
    /**
     * Position of the variant, 0-based
     */
    private long pos;
    /**
     * rs ID of the variant
     */
    private String id;
    /**
     * Reference sequence
     */
    private String ref;
    /**
     * Alternative alleles in cluster
     */
    private String alt;

    /**
     * hgvs string
     */
    private String hgvs;

    /**
     * {@link DiseaseInfo} info about this variant
     */
    private DiseaseInfo clinvar;

    private HashMap<String, PopulationInfo> population = new HashMap<>();

    /**
     * {@link EffectInfo} info about this variant
     */
    private EffectInfo effect;

    /**
     * {@link ScoreInfo} info about this variant
     */
    private ScoreInfo scores;

    private String gene;

    private ACMGRecord acmg;

}
