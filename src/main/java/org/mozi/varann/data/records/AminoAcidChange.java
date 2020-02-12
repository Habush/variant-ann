package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embedded
@AllArgsConstructor
@NoArgsConstructor
public class AminoAcidChange {

    private String gene;
    private String transcriptId;
    private String exon;
    private String cdsChange;
    private String proteinChange;

}
