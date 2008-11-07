package com.sun.labs.util.props;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A global property of the sphinx configuration system
 *
 * @author Holger Brandl
 */
public class GlobalProperty {

    // this pattern matches strings of the form '${word}'
    protected static Pattern globalSymbolPattern =
            Pattern.compile("\\$\\{([\\w\\.]+)\\}");

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

    Object value;


    public GlobalProperty(Object value) {
        this.value = value;
    }


    public Object getValue() {
        return value;
    }


    public void setValue(Object value) {
        this.value = value;
    }


    public String toString() {
        return value != null ? value.toString() : null;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalProperty)) return false;

        GlobalProperty that = (GlobalProperty) o;

//        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        // note: will be fixed as soon as we have typed global properties

        return true;
    }


    public int hashCode() {
        return (value != null ? value.hashCode() : 0);
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
