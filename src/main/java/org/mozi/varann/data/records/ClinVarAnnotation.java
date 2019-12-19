package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mozi.varann.data.impl.clinvar.ClinVarOrigin;
import org.mozi.varann.data.impl.clinvar.ClinVarSourceInfo;

import java.util.List;

/**
 * One annotation entry
 * <p>
 * One allele can have multiple annotations.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@Embedded
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClinVarAnnotation {

	/**
	 * Mapping to allele, -1 is no mapping, 0 is reference, 1 is first alt
	 */
	private int alleleMapping;

	/**
	 * HGVS variant string
	 */
	private String hgvsVariant;

	/**
	 * Clinvar variant source
	 */
	private List<ClinVarSourceInfo> sourceInfos;

	/**
	 * Origin of the annotation
	 */
	private List<ClinVarOrigin> origin;

	/**
	 * Clinvar disease db informations
	 */
	private List<ClinVarDiseaseInfo> diseaseInfos;

}
