package com.sun.labs.util.props;

/**
 * An interface that can be used for changes to components that have been 
 * looked up in a configuration manager, especially for those components that 
 * have been looked up in a component registry.
 * 
 * @see ConfigurationManager#lookup(String,ComponentListener)
 * @see ConfigurationManager#lookupAll(Class, ComponentListener)
 */
public interface ComponentListener {
    
    
    /**
     * Indicates that a component has been added to the configuration.  This
     * method will only be called when a component of a type that the listener
     * looked up has been added.  Note that this method will be called with any
     * component of the appropriate type.  It is up to the implementer to decide
     * whether it is interested in any particular instance of a component.
     * @param c the component that was added
     */
    public void componentAdded(Configurable c);
    
    /**
     * Indicates that a component has been removed from the configuration.  This 
     * method will be called when any component of a type that the listener looked
     * up has been removed.
     * @param c the component that was removed.  Note that it's not likely that you
     * can do much with this component except test it's class.
     */
    public void componentRemoved(Configurable c);
    
}
