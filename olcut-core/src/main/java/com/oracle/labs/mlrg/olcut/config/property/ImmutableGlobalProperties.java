package com.oracle.labs.mlrg.olcut.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * A collection of GlobalProperties which can't be mutated.
 */
public class ImmutableGlobalProperties implements Iterable<Map.Entry<String,GlobalProperty>> {

    /**
     * A set of distinguished properties that we would like to have.
     */
    private static Map<String, GlobalProperty> distinguished = new HashMap<>();

    protected final HashMap<String, GlobalProperty> map;

    static {
        distinguished.put("gp.hostName", new LazyGlobalProperty(ConfigurationManagerUtils::getHostName));
    }

    public ImmutableGlobalProperties() {
        this.map = new HashMap<>();
    }

    public ImmutableGlobalProperties(GlobalProperties globalProperties) {
        this.map = new HashMap<>();
        for(String key : globalProperties.map.keySet()) {
            map.put(key, new GlobalProperty(globalProperties.get(key)));
        }
    }

    private ImmutableGlobalProperties(HashMap<String, GlobalProperty> map) {
        this.map = map;
    }

    public GlobalProperty get(String propertyName) {
        GlobalProperty gp = map.get(propertyName);
        if(gp == null) {
            gp = distinguished.get(propertyName);
        }
        return gp;
    }

    // todo implement hashCode
    public boolean equals(Object o) {
        if(o instanceof ImmutableGlobalProperties) {
            ImmutableGlobalProperties gp = (ImmutableGlobalProperties) o;
            if(!map.keySet().equals(gp.map.keySet())) {
                return false;
            }

            //compare all values
            for (String key : gp.map.keySet()) {
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
        boolean matched = false;
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            matched = true;
            //
            // Get the recursive replacement for this value.
            GlobalProperty prop = get(m.group(1));
            String replace = prop == null ? null : prop.getValue();
            if(replace == null) {
                throw new PropertyException(instanceName, propName,
                        "Unknown global property:  " +
                                m.group(0));
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(replace));
        }
        m.appendTail(sb);
        if (matched) {
            return replaceGlobalProperties(instanceName,propName,sb.toString());
        } else {
            return sb.toString();
        }
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public ImmutableGlobalProperties getImmutableProperties() {
        return new ImmutableGlobalProperties(map);
    }

    @Override
    public Iterator<Map.Entry<String, GlobalProperty>> iterator() {
        return map.entrySet().iterator();
    }

}
