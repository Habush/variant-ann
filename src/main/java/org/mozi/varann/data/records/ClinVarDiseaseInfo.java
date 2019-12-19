package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Disease-specific information in a {@link ClinVarAnnotation}
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@Embedded
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClinVarDiseaseInfo {
	/**
	 * Significance level
	 */
	private List<String> significance;

	/**
	 * ID of disease in DB
	 */
	private String diseaseDBID;

	/**
	 * Name of disease in DB
	 */
	private String diseaseDBName;

	/**
	 * Revision status of the variant
	 */
	private List<String> revisionStatus;
}
