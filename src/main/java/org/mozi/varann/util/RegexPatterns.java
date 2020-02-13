package org.mozi.varann.util;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/28/20
 */
public class RegexPatterns {
    public static final Pattern rangePattern = Pattern.compile("(?:chr)?([\\d|XYMTxymt]+):(\\d+)-(\\d+)");
    public static final Pattern substitutionPattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)([GCTAgcta])?>([GCTAgcta])");
    public static final Pattern substitutionNoChangePattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)([gctaGCTA])?=");
    public static final Pattern indelPattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)(?:_(\\d+))?([GCTAgcta]+)?delins([GCTAgcta]+)");
    public static final Pattern insPattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)_(\\d+)ins([GCTAgcta]+)");
    public static final Pattern delPattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)(?:_(\\d+))?del([GCTAgcta]+)?");
    public static final Pattern insInvertedPattern = Pattern.compile("(chr)?([\\d|XYMTxymt]+):[gG]\\.(\\d+)_(\\d+)ins(\\d+)_(\\d+)inv");

    public static boolean hgvsMatch(String hgvs) {
        return substitutionPattern.matcher(hgvs).matches() || substitutionNoChangePattern.matcher(hgvs).matches() || indelPattern.matcher(hgvs).matches()
                || insPattern.matcher(hgvs).matches() || delPattern.matcher(hgvs).matches()
                || insInvertedPattern.matcher(hgvs).matches();
    }
}
