package org.mozi.varann.data.records;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/23/19
 */
public enum PredictedEffect {
    UNCERTAIN_SIGNIFICANCE,
    NEUTRAL,
    TOLERATED,
    BENIGN,
    PATHOGENIC,
    DELETERIOUS;

    public static PredictedEffect fromString(String eff) {
        switch (eff){
            case "U":
                return UNCERTAIN_SIGNIFICANCE;
            case "N":
                return NEUTRAL;
            case "T":
                return TOLERATED;
            case "B":
                return BENIGN;
            case "P":
                return PATHOGENIC;
            case "D":
                return DELETERIOUS;
            default:
                throw new RuntimeException("Unknown predicted effect");
        }
    }

}
