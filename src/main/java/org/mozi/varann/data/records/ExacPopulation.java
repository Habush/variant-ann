package org.mozi.varann.data.records;

/**
 * Enum type for populations in the ExAC data set
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public enum ExacPopulation {
	/**
	 * African/African American
	 */
	afr,
	/**
	 * American
	 */
	amr,
	/**
	 * East Asian
	 */
	eas,
	/**
	 * Finish
	 */
	fin,
	/**
	 * Non-Finnish European
	 */
	nfe,
	/**
	 * Other population
	 */
	oth,
	/**
	 * South asian population
	 */
	sas,
	/**
	 * Pseudo-population meaning "all pooled together"
	 */
	all;

	public String getLabel() {
		switch (this) {
			case afr:
				return "African/African American";
			case amr:
				return "American";
			case fin:
				return "Finnish";
			case nfe:
				return "Non-Finnish European";
			case oth:
				return "Other";
			case sas:
				return "South Asian";
			case eas:
				return "East Asian";
			case all:
				return "All";
			default:
				return "Undefined";
		}
	}
}
