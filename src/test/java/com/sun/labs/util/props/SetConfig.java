package com.sun.labs.util.props;

import java.util.Set;

/**
 *
 */
public class SetConfig implements Configurable {

    @Config(genericType=java.lang.String.class)
    public Set<String> stringSet;

    @Config(genericType=java.lang.Double.class)
    public Set<Double> doubleSet;

    @Config(genericType=StringConfigurable.class)
    public Set<StringConfigurable> stringConfigurableSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetConfig setConfig = (SetConfig) o;

        if (stringSet != null ? !stringSet.equals(setConfig.stringSet) : setConfig.stringSet != null) return false;
        if (doubleSet != null ? !doubleSet.equals(setConfig.doubleSet) : setConfig.doubleSet != null) return false;
        return stringConfigurableSet != null ? stringConfigurableSet.equals(setConfig.stringConfigurableSet) : setConfig.stringConfigurableSet == null;
    }

    @Override
    public int hashCode() {
        int result = stringSet != null ? stringSet.hashCode() : 0;
        result = 31 * result + (doubleSet != null ? doubleSet.hashCode() : 0);
        result = 31 * result + (stringConfigurableSet != null ? stringConfigurableSet.hashCode() : 0);
        return result;
    }
}
