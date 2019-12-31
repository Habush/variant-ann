package org.mozi.varann.data.records;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.mozi.varann.data.impl.dbsnp.DBSNPVariantProperty;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Entity("effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantEffectRecord extends BaseRecord {
    /**
     * Mongo Id
     */
    @Id
    @JsonIgnore
    private ObjectId _id;

    // Fields up to the INFO column

    /**
     dbSNP rs id
     */
    @JsonIgnore
    public String rsId;

    /**
     * Annotation for the variant
     */
    private HashMap<String, Collection<AnnotationRecord>> annotation;

    private HashMap<String, Collection<String>> hgvsNomination;

    // Fields up to the INFO column
    /**
     * Filters, NC: inconsistent genotype submission for at least one sample
     */
    private List<String> filter = new ArrayList<>();

    // Entries of the INFO column

    /**
     *  RS cluster ID
     */
    /**
     * Position of the cluster
     */
    private int rsPos;
    /**
     * Whether or not the RS orientation is reversed
     */
    private boolean reversed;
    /**
     * Representation of the dbSNP bit flags
     */
    private DBSNPVariantProperty variantProperty;
    /**
     * ID of first dbSNP build where this variant appears
     */
    private int dbSNPBuildID;
    /**
     * Variant origin (germline/somatic)
     */
    private String origin;
    /**
     * Explanation of possible suspectiveness
     */
    private HashSet<String> variantSuspectReasonCode = new HashSet<>();
    /**
     * Weight of the variant
     */
    private int weights;
    /**
     * Class of the variation
     */
    private String variationClass;
    /**
     * Whether the variant is precious (clinical or pubmed cited)
     */
    boolean precious;
    /**
     * Has third-party annotation
     */
    boolean thirdPartyAnnotation;
    /**
     * Has pub med central citation
     */
    boolean pubMedCentral;
    /**
     * Has 3D structure information
     */
    boolean threeDStructure;
    /**
     * Has submitter link-out
     */
    boolean submitterLinkOut;
    /**
     * Has non-synonymous frameshift effect
     */
    boolean nonSynonymousFrameShift;
    /**
     * Has non-synonymous missense effect
     */
    boolean nonSynonymousMissense;
    /**
     * Has non-synonymous nonsense effect
     */
    boolean nonSynonymousNonsense;
    /**
     * Coding variant with one allele being reference
     */
    boolean reference;
    /**
     * Coding variant with synonymous effect
     */
    boolean synonymous;
    /**
     * Is in 3' UTR
     */
    boolean inThreePrimeUTR;
    /**
     * Is in 5' UTR
     */
    boolean inFivePrimeUTR;
    /**
     * Is in splice acceptor site
     */
    boolean inAcceptor;
    /**
     * Is in splice donor site
     */
    boolean inDonor;
    /**
     * Is in intron
     */
    boolean inIntron;
    /**
     * Is in 3' gene region
     */
    boolean inThreePrime;
    /**
     * Is in 5' gene region
     */
    boolean inFivePrime;
    /**
     * Has other variatn with exactly same set of mapped positions
     */
    boolean otherVariant;
    /**
     * Has assembly conflict
     */
    boolean assemblyConflict;
    /**
     * Is assembly specific
     */
    boolean assemblySpecific;
    /**
     * Is known mutation (journal citation, explicit fact), low-frequency
     */
    boolean mutation;
    /**
     * Has been validated
     */
    boolean validated;
    /**
     * Has >5% minor allele frequency in each and all populations
     */
    boolean fivePercentAll;
    /**
     * Has >5% minor allele frequency in >=1 population
     */
    boolean fivePercentOne;
    /**
     * Marker is on high-density genotyping kit
     */
    boolean highDensityGenotyping;
    /**
     * Individual genotypes available
     */
    boolean genotypesAvailable;
    /**
     * Is in 1000 genomes phase 1 list
     */
    boolean g1kPhase1;
    /**
     * Is in 1000 genomes phase 3 list
     */
    boolean g1kPhase3;
    /**
     * Is interrogated in clinical diagnostic assay
     */
    boolean clinicalDiagnosticAssay;
    /**
     * Comes from locus-specific database
     */
    boolean locusSpecificDatabase;
    /**
     * Microattribution/thirdy-aprty annotation
     */
    boolean microattributionThirdParty;
    /**
     * Has OMIM/OMIA id
     */
    boolean hasOMIMOrOMIA;
    /**
     * Contig allele not present in variant allele list
     */
    boolean contigAlelleNotVariant;
    /**
     * Has been withdrawn by submitter
     */
    boolean withdrawn;
    /**
     * NonHas non-overlapping allele sets
     */
    boolean nonOverlappingAlleleSet;
    /**
     * Alternative frequencies as seen in 1000 Genomes project, entry with index 0 is the first alternative allele
     */
    List<Double> alleleFrequenciesG1K = new ArrayList<>();
    /**
     * Is a common SNP (>=1% in at least one 1000 genomes population with at least 2 founders contributing)
     */
    boolean common;
    /**
     * List of information on old variants
     */
    List<String> oldVariants = new ArrayList<>();
}
