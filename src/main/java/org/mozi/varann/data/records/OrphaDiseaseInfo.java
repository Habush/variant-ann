package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/20/20
 */
@Data
@Embedded
@AllArgsConstructor
@NoArgsConstructor
public class OrphaDiseaseInfo {
    private String diseaseId;
    private String diseaseName;
    private String prevalence;
    private String inheritance;
    private String ageOnset;
    private String[] omimIds;
}
