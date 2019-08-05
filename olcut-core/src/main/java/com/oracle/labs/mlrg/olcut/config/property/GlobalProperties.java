package com.oracle.labs.mlrg.olcut.config.property;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * A collection of global properties used within a system configuration
 *
 * @see ConfigurationManager
 */
public class GlobalProperties extends ImmutableGlobalProperties {

    public GlobalProperties() {
        super();
    }

    public GlobalProperties(GlobalProperties globalProperties) {
        super(globalProperties);
    }

    /**
     * Imports the system properties into GlobalProperties.
     */
    public final void importSystemProperties() {
        Properties props = AccessController.doPrivileged((PrivilegedAction<Properties>) System::getProperties);
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            String param = (String) e.getKey();
            String value = (String) e.getValue();
            setValue(param, value);
        }
    }

    /**
     * Adds a value to this GlobalProperties. Throws PropertyException if the
     * name does not conform to the {@link GlobalProperty#globalSymbolPattern}.
     *
     * It overwrites values if they already exist.
     * @param propertyName The name of the new global property.
     * @param value The value for the new global property.
     * @throws PropertyException If the name is invalid.
     */
    public void setValue(String propertyName, String value) throws PropertyException {
        setValue(propertyName, new GlobalProperty(value));
    }

    /**
     * Adds a value to this GlobalProperties. Throws PropertyException if the
     * name does not conform to the {@link GlobalProperty#globalSymbolPattern}.
     *
     * It overwrites values if they already exist.
     * @param propertyName The name of the new global property.
     * @param value The value for the new global property.
     * @throws PropertyException If the name is invalid.
     */
    public void setValue(String propertyName, GlobalProperty value) throws PropertyException {
        String testValue = "${" + propertyName + "}";
        Matcher m = GlobalProperty.globalSymbolPattern.matcher(testValue);
        if (!m.matches()) {
            throw new PropertyException("GlobalProperties",propertyName,"Does not conform to the GlobalProperty regex");
        }
        map.put(propertyName, value);
    }

    /**
     * Adds all the global properties from the other {@link GlobalProperties},
     * overwriting properties with the same name.
     * @param otherGP The global properties to add.
     */
    public void putAll(GlobalProperties otherGP) {
        for (Map.Entry<String,GlobalProperty> p : otherGP) {
            map.put(p.getKey(),p.getValue());
        }
    }

    /**
     * Removes the specified global property if it exists.
     * @param key The property name to remove.
     */
    public void remove(String key) {
        map.remove(key);
    }
}
