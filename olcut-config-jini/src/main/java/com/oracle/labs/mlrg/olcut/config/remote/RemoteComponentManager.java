
package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.ComponentListener;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;

import java.io.Closeable;

/**
 * Manages connecting and reconnecting to a component
 */
public class RemoteComponentManager<T extends Configurable> implements Closeable, ComponentListener<T> {
    protected ConfigurationManager cm;
    protected Class<T> clazz;
    private T component = null;

    /**
     * Creates a RemoteComponentManager
     * @param cm the configuration manager to use to fetch components
     * @param c The class this component manager looks after.
     */
    public RemoteComponentManager(ConfigurationManager cm, Class<T> c) {
        this.cm = cm;
        this.clazz = c;
    }

    private T lookup() {
        return cm.lookup(clazz, this);
    }

    /**
     * Gets the component with the given name
     * @return the component
     */
    public T getComponent() {
        if(component == null) {
            component = cm.lookup(clazz, this);
        }
        return component;
    }

    @Override
    public void componentAdded(T c) {
        if(component == null && clazz.isAssignableFrom(c.getClass())) {
            component = c;
        }
    }

    @Override
    public void componentRemoved(T componentToRemove) {
        if (component == componentToRemove) {
            component = null;
        }
    }

    @Override
    public void close() {
        cm.close();
    }
}
