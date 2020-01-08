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
 * 12/23/19
 */
@EqualsAndHashCode(callSuper = true)
@Entity("dbnsfp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBNSFPRecord extends BaseRecord {

    @Id
    @JsonIgnore
    private ObjectId _id;

    // Fields up to the INFO column

    /**
     * SIFT scores
     */
    private List<Double> sift = new ArrayList<>();

    private List<String> siftPred = new ArrayList<>();


    /**
     * CADD scores
     */
    private List<Double> cadd = new ArrayList<>();
    /**
     * PloyPhen2 scores
     */
    private List<Double> polyphen2 = new ArrayList<>();

    private List<String> polyphen2Pred = new ArrayList<>();

    /**
     * LRT scores
     */
    private List<Double> lrt = new ArrayList<>();
    private List<String> lrtPred = new ArrayList<>();
    /**
     * MutationTaster scores
     */
    private List<Double> mutationTaster = new ArrayList<>();
    private List<String> mutationTasterPred = new ArrayList<>();
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
        this.siftPred.addAll(other.getSiftPred());
        this.cadd.addAll(other.getCadd());
        this.polyphen2.addAll(other.getPolyphen2());
        this.polyphen2Pred.addAll(other.getPolyphen2Pred());
        this.lrt.addAll(other.getLrt());
        this.lrtPred.addAll(other.getLrtPred());
        this.mutationTaster.addAll(other.getMutationTaster());
        this.mutationTasterPred.addAll(other.getMutationTasterPred());
        this.dann.addAll(other.getDann());
        this.vest4.addAll(other.getVest4());
    }



}
