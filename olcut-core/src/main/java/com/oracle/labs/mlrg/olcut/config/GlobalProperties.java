package com.oracle.labs.mlrg.olcut.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * A collection of global properties used within a system configuration
 *
 * @see ConfigurationManager
 */
public class GlobalProperties extends HashMap<String, GlobalProperty> {

    /**
     * A set of distinguished properties that we would like to have.
     */
    private static Map<String, GlobalProperty> distinguished = new HashMap<>();

    static {
        distinguished.put("gp.hostName", new LazyGlobalProperty(ConfigurationManagerUtils::getHostName));
        distinguished.put("gp.username", new LazyGlobalProperty(ConfigurationManagerUtils::getUserName));
    }

    public GlobalProperties() { }

    public GlobalProperties(GlobalProperties globalProperties) {
        for(String key : globalProperties.keySet()) {
            put(key, new GlobalProperty(globalProperties.get(key)));
        }
    }

    public void setValue(String propertyName, String value) {
        if(keySet().contains(propertyName)) {
            get(propertyName).setValue(value);
        } else {
            put(propertyName, new GlobalProperty(value));
        }
    }
    
    public GlobalProperty get(String propertyName) {
        GlobalProperty gp = super.get(propertyName);
        if(gp == null) {
            gp = distinguished.get(propertyName);
        }
        return gp;
    }
    
    // todo implement hashCode
    public boolean equals(Object o) {
        if(o != null && o instanceof GlobalProperties) {
            GlobalProperties gp = (GlobalProperties) o;
            if(!keySet().equals(gp.keySet())) {
                return false;
            }

            //compare all values
            for(String key : gp.keySet()) {
                if(!get(key).equals(gp.get(key))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
    
    /**
     * Replaces all of the global properties in a property value with the appropriate
     * global values.
     * @param instanceName the name of the instance whose properties we're 
     * processing.
     * @param propName the name of the property whose value we're processing
     * @param val the property value
     * @return the property value with all global properties replaced with their
     * corresponding values.
     */
    protected String replaceGlobalProperties(String instanceName,
                                            String propName, String val) {
        Matcher m = GlobalProperty.globalSymbolPattern.matcher(val);
        StringBuffer sb = new StringBuffer();
        while(m.find()) {
            //
            // Get the recursive replacement for this value.
            GlobalProperty prop = get(m.group(1));
            String replace = prop == null ? null : prop.getValue();
            if(replace == null) {
                throw new PropertyException(instanceName, propName,
                                            "Unknown global property:  " +
                                            m.group(0));
            }
            
            //
            // We may need to recursively replace global properties embedded in
            // this value.
            if(GlobalProperty.hasGlobalProperty(replace)) {
                replace = replaceGlobalProperties(instanceName, propName, replace);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replace));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
