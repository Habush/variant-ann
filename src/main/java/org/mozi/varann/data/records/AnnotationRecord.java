package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embedded
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationRecord {

    private String consquence;

    private String impact;

    private String featureType;

    private String featureId;

    private String bioType;

    private String cdsChange;

    private String proteinChange;

}
