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
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/13/20
 */

@Entity("variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantRecord {
    @Id
    @JsonIgnore
    private ObjectId _id;
    /**
     * Name of the chromosome
     */
    @JsonIgnore
    public String chrom;
    /**
     * Position of the variant, 0-based
     */
    @JsonIgnore
    public long pos;
    /**
     * Reference sequence
     */
    @JsonIgnore
    public String ref;
    /**
     * Alternative alleles in cluster
     */
    @JsonIgnore
    public String alt;

    /**
     * Hgvs strings for each alternate allele
     */
    @JsonIgnore
    public String hgvs;
    private String rsId;

    private String bioType;


    private String exonicFunction;

    private String gene;

    private List<String> geneId;

    private List<AminoAcidChange> ensAAChange = new ArrayList<>();

    private List<AminoAcidChange> refAAChange = new ArrayList<>();

    private GnomadGenomeRecord gnomeRecord;

    private DBNSFPRecord dbnsfpRecord;
}
