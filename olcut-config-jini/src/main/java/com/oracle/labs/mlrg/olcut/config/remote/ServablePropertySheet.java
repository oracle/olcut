package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.ComponentListener;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;

import java.rmi.Remote;
import java.util.logging.Logger;

/**
 * A property sheet which defines a collection of properties for a single
 * component in the system.
 */
public class ServablePropertySheet<T extends Configurable> extends PropertySheet<T> {
    private static final Logger logger = Logger.getLogger(ServablePropertySheet.class.getName());

    private final boolean implementsRemote;

    /**
     * The configuration entries to use for service registration or matching.
     */
    private final ConfigurationEntry[] entries;

    protected ServablePropertySheet(T configurable, String name,
                                 JiniConfigurationManager cm, ConfigurationData rpd) {
        this((Class<T>) configurable.getClass(), name, cm, rpd);
        owner = configurable;
    }

    protected ServablePropertySheet(Class<T> confClass, String name,
                                 JiniConfigurationManager cm, ConfigurationData rpd) {
        super(confClass,name,cm,rpd);

        //
        // Does this class implement remote?
        implementsRemote = Remote.class.isAssignableFrom(ownerClass);

        //
        // If we're supposed to have configuration entries, then get them now.
        if (entriesName != null) {
            ConfigurationEntries ce
                    = (ConfigurationEntries) cm.lookup(entriesName);
            if (ce == null) {
                throw new PropertyException(instanceName, "entries",
                        "Cannot find entries component " + entriesName);
            }
            entries = ce.getEntries();
        } else {
            entries = null;
        }
    }

    public ServablePropertySheet(ServablePropertySheet<T> other){
        super(other);

        //
        // Does this class implement remote?
        implementsRemote = Remote.class.isAssignableFrom(other.ownerClass);

        //
        // If we're supposed to have configuration entries, then get them now.
        if (entriesName != null) {
            ConfigurationEntries ce
                    = (ConfigurationEntries) cm.lookup(entriesName);
            if (ce == null) {
                throw new PropertyException(instanceName, "entries",
                        "Cannot find entries component " + entriesName);
            }
            entries = ce.getEntries();
        } else {
            entries = null;
        }
    }

    public boolean implementsRemote() {
        return implementsRemote;
    }

    public ConfigurationEntry[] getEntries() {
        return entries;
    }

    @Override
    public synchronized T getOwner(ComponentListener<T> cl, boolean reuseComponent) {
        if (!isInstantiated() || !reuseComponent) {

            ComponentRegistry registry = ((JiniConfigurationManager)cm).getComponentRegistry();
            //
            // See if we should do a lookup in a service registry.
            if (registry != null
                    && !isExportable()
                    && ((size() == 0 && implementsRemote) || isImportable())) {
                    logger.finer(String.format("Looking up instance %s in registry",
                            getInstanceName()));
                owner = registry.lookup(this, cl);
                if (owner != null) {
                    return owner;
                } else if (size() == 0 && isImportable()) {
                    //
                    // We needed to look something up and no success,
                    // so throw exception.
                    throw new PropertyException(instanceName,"Failed to lookup instance.");
                }
            }

            // Failed to do service lookup, instantiate directly
            super.getOwner(cl, reuseComponent);

            if (registry != null && isExportable()) {
                registry.register(owner, this);
            }
        }

        return owner;
    }

    /**
     * Gets the owning property manager
     *
     * @return the property manager
     */
    @Override
    public JiniConfigurationManager getConfigurationManager() {
        return (JiniConfigurationManager) cm;
    }

    @Override
    public void setCM(ConfigurationManager cm) {
        if (cm instanceof JiniConfigurationManager) {
            this.cm = cm;
        } else {
            throw new IllegalArgumentException("Must pass a JiniConfigurationManager to a ServablePropertySheet");
        }
    }

    @Override
    public ServablePropertySheet<T> copy() {
        return new ServablePropertySheet<>(this);
    }

}
