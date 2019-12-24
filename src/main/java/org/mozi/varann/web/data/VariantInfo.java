package org.mozi.varann.web.data;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import lombok.Data;
import org.mozi.varann.data.records.*;

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
    private int pos;
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
     * Filters, NC: inconsistent genotype submission for at least one sample
     */


//    /**
//     * {@link DBSNPRecord} info about this variant
//     */
//    private DBSNPRecord dbsnp;

    /**
     * {@link DiseaseInfo} info about this variant
     */
    private DiseaseInfo disease;

    /**
     * {@link ThousandGenomesRecord} info about this variant
     */
    private ThousandGenomesRecord thousandGenome;

    /**
     * {@link ExacRecord} info about this variant
     */
    private ExacRecord exac;

    /**
     * {@link EffectInfo} info about this variant
     */
    private EffectInfo effect;

    private DBNSFPRecord scores;

}
