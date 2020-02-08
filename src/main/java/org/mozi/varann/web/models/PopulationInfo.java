package org.mozi.varann.web.models;

import lombok.Data;
import lombok.NoArgsConstructor;

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
