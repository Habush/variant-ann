package org.mozi.varann.web.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.mozi.varann.data.records.ThousandGenomesRecord;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/23/20
 */
@Data
@NoArgsConstructor
public class PopulationInfo {
    private PopulationData thousandGenome;
    private PopulationData exac;
    private PopulationData gnomadExome;
}
