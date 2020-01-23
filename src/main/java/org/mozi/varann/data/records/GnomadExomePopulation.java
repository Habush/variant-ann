package org.mozi.varann.data.records;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/22/20
 */
public enum GnomadExomePopulation {
    /**
     * African/African American
     */
    afr,
    /**
     * Latino
     */
    amr,
    /**
     * Ashkenazi Jewish
     */
    asj,
    /**
     * East Asian
     */
    eas,
    /**
     * Finnish
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
     * Japanese Population
     */
    eas_jpn,
    /**
     * Pseudo-population meaning "all pooled together"
     */
    all;

    public String getLabel() {
        switch (this) {
            case afr:
                return "African/African American";
            case amr:
                return "Latino";
            case fin:
                return "Finnish";
            case nfe:
                return "Non-Finnish European";
            case oth:
                return "Other";
            case asj:
                return "Ashkenazi Jewish";
            case eas_jpn:
                return "Japanese";
            case sas:
                return "South Asian";
            case all:
                return "All";
            default:
                return "Undefined";
        }
    }
}
