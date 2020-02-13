package org.mozi.varann.web.models;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */

import lombok.Data;
import org.mozi.varann.data.records.*;

import java.util.HashMap;
import java.util.List;

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

    private String bioType;

    private String gene;
    private List<String> ensembleGenes;
    private List<AminoAcidChange> ensembleTranscripts;
    private List<AminoAcidChange> refSeqTranscripts;
    private String exonicFunction;

    /**
     * {@link DiseaseInfo} info about this variant
     */
    private DiseaseInfo clinvar;

    private HashMap<String, PopulationInfo> population = new HashMap<>();


    /**
     * {@link ScoreInfo} info about this variant
     */
    private ScoreInfo scores;

    private IntervarRecord acmg = new IntervarRecord();

}
