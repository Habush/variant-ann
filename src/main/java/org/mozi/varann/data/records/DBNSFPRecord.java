package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/23/19
 */
@Entity("dbnsfp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBNSFPRecord {

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
     * Hgvs string
     */
    @JsonIgnore
    private List<String> hgvs = new ArrayList<>();

    /**
     * SIFT scores
     */
    private List<PredictedScore> sift = new ArrayList<>();

    /**
     * CADD scores
     */
    private List<Double> cadd = new ArrayList<>();
    /**
     * PloyPhen2 scores
     */
    private List<PredictedScore> polyphen2 = new ArrayList<>();

    /**
     * LRT scores
     */
    private List<PredictedScore> lrt = new ArrayList<>();
    /**
     * MutationTaster scores
     */
    private List<PredictedScore> mutationTaster = new ArrayList<>();
    /**
     * DANN scores
     */
    private List<Double> dann = new ArrayList<>();
    /**
     * VEST4 scores
     */
    private List<Double> vest4 = new ArrayList<>();


    public void copy(DBNSFPRecord other) {
        this.alt.addAll(other.getAlt());
        this.hgvs.addAll(other.getHgvs());
        this.sift.addAll(other.getSift());
        this.cadd.addAll(other.getCadd());
        this.polyphen2.addAll(other.getPolyphen2());
        this.lrt.addAll(other.getLrt());
        this.mutationTaster.addAll(other.getMutationTaster());
        this.dann.addAll(other.getDann());
        this.vest4.addAll(other.getVest4());
    }



}
