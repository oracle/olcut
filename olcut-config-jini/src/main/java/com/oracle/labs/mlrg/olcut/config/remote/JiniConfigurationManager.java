package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.ComponentListener;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Options;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.ArgumentException;
import com.oracle.labs.mlrg.olcut.config.InternalConfigurationException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.UsageException;

import java.net.URL;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a set of <code>Configurable</code>s, their parametrization and the relationships between them. Configurations
 * can be specified either by a configuration file or on-the-fly during runtime.
 *
 * This configuration manager has a Jini component registry and can look up or serve classes over the network.
 *
 * @see Configurable
 */
public class JiniConfigurationManager extends ConfigurationManager {
    private static final Logger logger = Logger.getLogger(JiniConfigurationManager.class.getName());

    private ComponentRegistry registry;

    /**
     * Creates a new empty jini configuration manager. This constructor is only of use in cases when a system configuration
     * is created during runtime.
     */
    public JiniConfigurationManager() {
        super();
    }

    /**
     * Creates a jini new configuration manager. Initial properties are loaded from the given location.
     *
     * @param location place to load initial properties from
     */
    public JiniConfigurationManager(String location) throws PropertyException {
        super(location);

        //
        // Look up our distinguished registry name.
        setUpRegistry();
    }

    /**
     * Creates a new jini configuration manager. Initial properties are loaded from the given location.
     *
     * @param url place to load initial properties from
     */
    public JiniConfigurationManager(URL url) throws PropertyException {
        super(url);

        //
        // Look up our distinguished registry name.
        setUpRegistry();
    }

    /**
     * Creates a new Jini configuration manager. Used when all the command line arguments are either: requests for the usage
     * statement, configuration file options, or unnamed.
     * @param arguments An array of command line arguments.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     */
    public JiniConfigurationManager(String[] arguments) throws UsageException, ArgumentException, PropertyException {
        this(arguments,EMPTY_OPTIONS);
    }

    /**
     * Creates a new Jini configuration manager. Used when all the command line arguments are either: requests for the usage
     * statement, configuration file options, or unnamed.
     * @param arguments An array of command line arguments.
     * @param defaultConfigFile Loads the config from the specified path if no config is supplied.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     */
    public JiniConfigurationManager(String[] arguments, String defaultConfigFile) throws UsageException, ArgumentException, PropertyException {
        this(arguments,EMPTY_OPTIONS,defaultConfigFile);
    }

    /**
     * Creates a new jini configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * - Instantiates a jini component registry.
     * @param arguments An array of command line arguments.
     * @param options An object to write the parsed argument values into.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     */
    public JiniConfigurationManager(String[] arguments, Options options) throws UsageException, ArgumentException, PropertyException {
        this(arguments,options,"");
    }

    /**
     * Creates a new jini configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * - Instantiates a jini component registry.
     * @param arguments An array of command line arguments.
     * @param options An object to write the parsed argument values into.
     * @param defaultConfigFile Loads the config from the specified path if no config is supplied.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     */
    public JiniConfigurationManager(String[] arguments, Options options, String defaultConfigFile) throws UsageException, ArgumentException, PropertyException {
        super(arguments,options,defaultConfigFile,true);

        //
        // Look up our distinguished registry name.
        setUpRegistry();
    }

    /**
     * Adds a set of properties at the given URL to the current configuration
     * manager.
     */
    @Override
    public void addProperties(URL url) throws PropertyException {
        super.addProperties(url);

        if (registry == null) {
            setUpRegistry();
        }
    }

    /**
     * Makes sure that if a component registry is defined it is instantiated and
     * configured before anything else is looked up.
     */
    private void setUpRegistry() {
        ConfigurationData registryData = configurationDataMap.get("registry");
        if (registryData != null) {
            String className = registryData.getClassName();
            try {
                Class<?> registryClass = Class.forName(className);
                if (ComponentRegistry.class.isAssignableFrom(registryClass)) {
                    registry = (ComponentRegistry) lookup("registry");
                }
            } catch (ClassNotFoundException e) {
                throw new PropertyException(e, "registry", "Class " + className + " not found");
            }
        }
    }
    
    /**
     * Gets a remote proxy suitable for passing to another object, if that is 
     * necessary.  Exporting the object is deemed necessary if the object to which 
     * we want to send the object is something that we looked up in the service
     * registrar.
     * 
     * If we haven't started a component registry, then the original object will
     * be returned.
     * 
     * @param r the object that we may want a proxy for
     * @param c the component to which we want to pass <code>r</code>
     * @return a proxy for the object, if one is required.
     */
    public Remote getRemote(Remote r, Configurable c) {
        if(registry == null) {
            return r;
        }
        return registry.getRemote(r, c);
    }
    
    /**
     * Gets a remote proxy suitable for passing over the wire.  If we haven't
     * started a component registry, then the original object will be returned.
     * Otherwise, we unconditionally return the remote handle for the object.
     * 
     * @param r the object that we want a proxy for
     * @return a proxy for the object, if one can be created
     */
    public Remote getRemote(Remote r) {
        if (registry == null) {
            return r;
        }
        return registry.getRemote(r);
    }
    
    /**
     * Indicates whether the given component was looked up in a service
     * registrar.
     * 
     * @param c the component to test.
     * @return <code>true</code> if the component was looked up in a service
     * registrar, <code>false</code> otherwise.
     */
    protected boolean wasLookedUp(Configurable c) {
        if(registry == null) {
            return false;
        }
        return registry.wasLookedUp(c);
    }
    
    /**
     * Shuts down the configuration manager, which just makes sure that any 
     * component registry that we may have is shut down.
     */
    @Override
    public synchronized void close() {
        if(registry != null) {
            logger.info("Shutting down registry");
            registry.close();
            registry = null;
        }
    }

    /**
     * Returns the property sheet for the given object instance
     *
     * @param instanceName the instance name of the object
     * @return the property sheet for the object.
     */
    @Override
    protected ServablePropertySheet<? extends Configurable> getPropertySheet(String instanceName) {
        if(!symbolTable.containsKey(instanceName)) {
            // if it is not in the symbol table, so construct
            // it based upon our raw property data
            ConfigurationData rpd = configurationDataMap.get(instanceName);
            if(rpd != null) {
                String className = rpd.getClassName();
                try {
                    Class<?> confClass = Class.forName(className);
                    if (Configurable.class.isAssignableFrom(confClass)) {
                        ServablePropertySheet<? extends Configurable> propertySheet = new ServablePropertySheet<>((Class<? extends Configurable>)confClass,this,rpd);
                        symbolTable.put(instanceName, propertySheet);
                    } else {
                        throw new PropertyException(rpd.getName(), "Class " + className + " does not implement Configurable.");
                    }
                } catch (ClassNotFoundException e) {
                    throw new PropertyException(e, rpd.getName(), "Class " + className + " not found");
                }
            }
        }

        return (ServablePropertySheet<? extends Configurable>) symbolTable.get(instanceName);
    }

    /**
     * Looks up a configurable component by name. If a component registry exists
     * in the current configuration manager, it will be checked for the given
     * component name. 
     * 
     * @param instanceName the name of the component
     * @param cl a listener for this component that is notified when components
     * are added or removed
     * @param reuseComponent if <code>true</code>, then if the component was 
     * previously created that component will be returned.  If false, then a 
     * new component will be created regardless of whether it had been created
     * before.
     * @return the component, or null if a component was not found.
     * @throws InternalConfigurationException If the requested object could not be properly created, or is not a
     *                                        configurable object, or if an error occurred while setting a component
     *                                        property.
     */
    @Override
    public Configurable lookup(String instanceName, ComponentListener cl, boolean reuseComponent)
            throws InternalConfigurationException {
        // apply all new properties to the model
        instanceName = getStrippedComponentName(instanceName);
        Configurable ret = null;
        
        //
        // Get the property sheet for this component.
        ServablePropertySheet<? extends Configurable> ps = getPropertySheet(instanceName);
        
        if (ps == null) {
            return null;
        }
        
        //
        // If we have a registry and there's no properties in the property sheet
        // for this instance, then try to look it up in the registry.  This is
        // not perfect, because it will force lookups for components that just
        // happen to not have any properties, but it saves us from looking up
        // components that have local configurations and sitting through the 
        // component lookup timeout for the registry.  Life ain't perfect, eh?
        //
        // We'll also try a lookup if one is suggested by the importable attribute
        // for a component.
        if (registry != null && !ps.isExportable() &&
                ((ps.size() == 0 && ps.implementsRemote()) || ps.isImportable())) {
            logger.log(Level.FINER, "Attempted to lookup in registry");
            ret = registry.lookup(ps, cl);
        }

        //
        // Need we look farther?
        if (ret == null) {
            ret = super.lookup(instanceName, cl, reuseComponent);

            //
            // Do we need to export this?
            if(ps.isExportable() && registry != null) {
                registry.register(ret, ps);
            }
        }

        return ret;
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @return a list of all the components with the given class name as their type.
     */
    @Override
    public <T extends Configurable> List<T> lookupAll(Class<T> c) {
        return lookupAll(c,null);
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @param cl a listener that will report when components of the given type
     * are added or removed
     * @return a list of all the components with the given class name as their type.
     */
    @Override
    public <T extends Configurable> List<T> lookupAll(Class<T> c, ComponentListener<T> cl) {

        List<T> ret = super.lookupAll(c);
        if ((ret.size() == 0) && (registry != null)) {
            //
            // If we have a registry, then do a lookup for all things of the
            // given type.
            T[] reg = registry.lookup(c, Integer.MAX_VALUE, cl);
            ret.addAll(Arrays.asList(reg));
        }

        return ret;
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @param cl a listener that will report when components of the given type
     * are added or removed
     * @param entries Used to filter the looked up components.
     * @return a list of all the components with the given class name as their type.
     */
    public <T extends Configurable> List<T> lookupAll(Class<T> c, ComponentListener<T> cl, ConfigurationEntry[] entries) {

        List<T> ret = super.lookupAll(c);
        if ((ret.size() == 0) && (registry != null)) {
            //
            // If we have a registry, then do a lookup for all things of the
            // given type.
            T[] reg = registry.lookup(c, Integer.MAX_VALUE, cl, entries);
            ret.addAll(Arrays.asList(reg));
        }

        return ret;
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @param cl a listener that will report when components of the given type
     * are added or removed
     * @param entries Used to filter the looked up components.
     * @return a list of all the components with the given class name as their type.
     */
    public <T extends Configurable> List<T> lookupAll(Class<T> c, ComponentListener<T> cl, String[] entries) {

        // Convert the entries from Strings into ConfigurationEntries
        ConfigurationEntry[] configEntries = null;
        if (entries != null) {
            configEntries = new ConfigurationEntry[entries.length];
            for (int i = 0; i < entries.length; i++) {
                configEntries[i] = new ConfigurationEntry(entries[i]);
            }
        }

        return lookupAll(c,cl,configEntries);
    }

    /**
     * Gets the component registry that is being used to register and lookup
     * components in a service registrar
     * @return the current component registry, or <code>null</code> if there 
     * isn't one.
     */
    public ComponentRegistry getComponentRegistry() {
        return registry;
    }

    /**
     * Test whether the given configuration manager instance equals this instance in terms of same configuration.
     * This equals implementation does not care about instantiation of components.
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof JiniConfigurationManager)) {
            return super.equals(obj);
        }

        JiniConfigurationManager cm = (JiniConfigurationManager) obj;

        Collection<String> setA = new HashSet<>(getComponentNames());
        Collection<String> setB = new HashSet<>(cm.getComponentNames());
        if(!setA.equals(setB)) {
            return false;
        }

        // make sure that all components are the same
        for(String instanceName : getComponentNames()) {
            ConfigurationData myData = configurationDataMap.get(instanceName);
            ConfigurationData otherData = cm.configurationDataMap.get(instanceName);

            if(!myData.equals(otherData)) {
                return false;
            }
        }

        // make sure that both configuration managers have the same set of global properties
        return cm.getImmutableGlobalProperties().equals(getImmutableGlobalProperties());
    }

    @Override
    protected <T extends Configurable> ServablePropertySheet<T> createPropertySheet(T conf, ConfigurationManager cm, ConfigurationData rpd) {
        return new ServablePropertySheet<>(conf,(JiniConfigurationManager)cm,rpd);
    }
}
