package org.mozi.varann.web.models;

import lombok.Data;
import org.mozi.varann.data.records.ClinVarAnnotation;

import java.util.Collection;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/24/19
 */
@Data
public class DiseaseInfo {

    private ClinVarAnnotation annotation;

    private Collection<String> pumeds;
}
