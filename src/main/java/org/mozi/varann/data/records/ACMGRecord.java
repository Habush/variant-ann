package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/13/20
 */
@EqualsAndHashCode(callSuper = true)
@Entity("acmg")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ACMGRecord extends BaseRecord {
    @Id
    @JsonIgnore
    private ObjectId _id;

    private String verdict;

    //Evidences
    private int pvs1;
    private int[] ps;
    private int[] pm;
    private int[] pp;
    private int ba1;
    private int[] bs;
    private int[] bp;

    //Additional Info

    private String exonicFunction;
    private List<OrphaDiseaseInfo> diseaseInfos;
}
