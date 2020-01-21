package org.mozi.varann.web.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.mozi.varann.data.records.PredictedScore;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/8/20
 */
@NoArgsConstructor
@Data
public class ScoreInfo {
    private PredictedScore sift;
    private PredictedScore polyphen2;
    private PredictedScore lrt;

    private PredictedScore cadd;
    private PredictedScore mutationTaster;
    private PredictedScore dann;
    private PredictedScore vest4;
}
