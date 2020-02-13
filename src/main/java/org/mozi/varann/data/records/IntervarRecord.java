package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Embedded;
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
 * 2/5/20
 */

@Entity("intervar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntervarRecord {

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

    private String gene;
    private List<String> geneId;
    private String verdict = "Uncertain Significance";
    //Evidences
    private int pvs1 = 0;
    private int[] ps = new int[4];
    private int[] pm = new int[5];
    private int[] pp = new int[5];
    private int ba1 = 0;
    private int[] bs = new int[4];
    private int[] bp = new int[7];

    //Additional Info
    private List<OrphaDiseaseInfo> diseaseInfos = new ArrayList<>();
}
