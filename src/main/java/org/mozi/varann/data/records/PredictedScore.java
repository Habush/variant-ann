package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/23/19
 */
@Embedded
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictedScore {

    private double score;
    private String prediction;
}
