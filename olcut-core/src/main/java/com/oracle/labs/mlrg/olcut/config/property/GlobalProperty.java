package com.oracle.labs.mlrg.olcut.config.property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A global property of the olcut configuration system.
 */
public class GlobalProperty {

    // this pattern matches strings of the form '${word}'
    public final static Pattern globalSymbolPattern =
            Pattern.compile("\\$\\{([\\w\\.-]+)\\}");

    /**
     * Strips the ${ and } off of a global symbol of the form ${symbol}.
     *
     * @param symbol the symbol to strip
     * @return the stripped symbol
     */
    public static String stripGlobalSymbol(String symbol) {
        Matcher matcher = globalSymbolPattern.matcher(symbol);
        if(matcher.matches()) {
            return matcher.group(1);
        } else {
            return symbol;
        }
    }

    protected String value = null;

    protected GlobalProperty() { }

    public GlobalProperty(GlobalProperty other) {
        this.value = other.value;
    }

    public GlobalProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * This uses getValue to ensure subclassing works.
     * @return True if the values of the two GlobalProperty objects are the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalProperty that = (GlobalProperty) o;
        String tmpValue = getValue();
        String otherValue = that.getValue();

        return tmpValue != null ? tmpValue.equals(otherValue) : otherValue == null;
    }

    /**
     * This uses getValue to ensure subclassing works.
     * @return The hashcode of the value.
     */
    @Override
    public int hashCode() {
        String tmpValue = getValue();
        return tmpValue != null ? tmpValue.hashCode() : 0;
    }

    /**
     * Tests whether the given string is a global property.  This requires a 
     * match against the whole string.
     * @param s the string to test
     * @return <code>true</code> if the entire string names a global property.
     */
    public static boolean isGlobalProperty(String s) {
        return globalSymbolPattern.matcher(s).matches();
    }
    
    /**
     * Tests whether a given string contains a global property reference.
     * 
     * @param s the string to test
     * @return <code>true</code> if the string contains at least one global
     * property reference, <code>false</code> otherwise.
     */
    public static boolean hasGlobalProperty(String s) {
        return globalSymbolPattern.matcher(s).find();
    }
    
    public static String getPropertyName(String gpn) {
        Matcher m = globalSymbolPattern.matcher(gpn);
        if(m.matches()) {
            return m.group(1);
        }
        return gpn;
    }
}
