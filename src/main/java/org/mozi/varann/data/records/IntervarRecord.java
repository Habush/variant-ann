package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/5/20
 */
@Embedded
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntervarRecord {

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
    private List<OrphaDiseaseInfo> diseaseInfos = new ArrayList<>();
}
