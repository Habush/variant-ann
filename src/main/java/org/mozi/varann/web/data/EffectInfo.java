package org.mozi.varann.web.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mozi.varann.data.records.AminoAcidChange;

import java.util.Collection;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/24/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EffectInfo {

    private Collection<AminoAcidChange> annotation;
    private Collection<String> hgvsNomination;
}
