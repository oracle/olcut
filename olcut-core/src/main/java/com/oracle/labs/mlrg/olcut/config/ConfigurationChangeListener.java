package com.oracle.labs.mlrg.olcut.config;

/**
 * Describes all methods necessary to process change events of a <code>ConfigurationManager</code>.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */

public interface ConfigurationChangeListener {

    /**
     * Called if the configuration of a registered component named <code>configurableName</code> was changed.
     *
     * @param configurableName The name of the changed configurable.
     * @param propertyName     The name of the property which was changed
     * @param cm               The <code>ConfigurationManager</code>-instance this component is registered to
     */
    public void configurationChanged(String configurableName, String propertyName, ConfigurationManager cm);


    /**
     * Called if a new component defined by <code>ps</code> was registered to the ConfigurationManager
     * <code>cm</code>.
     */
    public void componentAdded(ConfigurationManager cm, PropertySheet ps);


    /**
     * Called if a component defined by <code>ps</code> was unregistered (removed) from the ConfigurationManager
     * <code>cm</code>.
     */
    public void componentRemoved(ConfigurationManager cm, PropertySheet ps);


    /**
     * Called if a component was renamed.
     */
    public void componentRenamed(ConfigurationManager cm, PropertySheet ps, String oldName);
}
