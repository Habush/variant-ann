package org.mozi.varann.data.records;

/**
 * Enum type for populations in the thousand genomes data set
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public enum ThousandGenomesPopulation {
	/**
	 * African/African American
	 */
	afr,
	/**
	 * American
	 */
	amr,
	/**
	 * Asian
	 */
	asn,
	/**
	 * European
	 */
	eur,
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
			case asn:
				return "Asian";
			case eur:
				return "European";
			case all:
				return "All";
			default:
				return "Undefined";
		}
	}
}
