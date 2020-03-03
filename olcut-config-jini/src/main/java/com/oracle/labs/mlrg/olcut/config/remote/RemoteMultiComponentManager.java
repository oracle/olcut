
package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.logging.Logger;

/**
 * A component manager that will handle multiple instances of a given component
 * type and hand them out in a round-robin fashion when requested.
 */
public class RemoteMultiComponentManager<T extends Configurable> extends RemoteComponentManager<T> {

    private T[] components;

    private int p;

    private static final Logger logger = Logger.getLogger(RemoteMultiComponentManager.class.getName());
    
    public RemoteMultiComponentManager(ConfigurationManager cm, Class<T> c) {
        super(cm, c);
    }

    private synchronized void getComponents() {
        List<T> l = cm.lookupAll(clazz, this);
        if(l != null) {
            components = l.toArray((T[])Array.newInstance(clazz, 0));
        }
    }
    
    /**
     * Gets the component with the given name
     * @return the component
     */
    public synchronized T getComponent() {
        if(components == null) {
            getComponents();
        }
        if (components.length == 0) {
            throw new PropertyException(new ClassNotFoundException("Error finding class " + clazz.getName()),"","Error finding instances of class " + clazz.getName());
        }
        p %= components.length;
        return components[p++];
    }

    @Override
    public void componentAdded(Configurable added) {
        logger.info("Added: " + added);
        getComponents();
    }

    @Override
    public void componentRemoved(Configurable componentToRemove) {
        logger.info("Removed: " + componentToRemove);
        getComponents();
    }

    @Override
    public void close() {
        cm.close();
    }
}
