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
 * 12/23/19
 */
@Embedded
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBNSFPRecord {
    /**
     * SIFT scores
     */
    private double sift;

    private String siftPred;


    /**
     * CADD scores
     */
    private double cadd ;
    /**
     * PloyPhen2 scores
     */
    private double polyphen2 ;

    /**
     * LRT scores
     */
    private double lrt;
    private String lrtPred;
    /**
     * MutationTaster scores
     */
    private double mutationTaster;

    private String mutationTasterPred;


}
