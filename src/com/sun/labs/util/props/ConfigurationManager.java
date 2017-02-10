package com.sun.labs.util.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanServer;

/**
 * Manages a set of <code>Configurable</code>s, their parametrization and the relationships between them. Configurations
 * can be specified either by xml or on-the-fly during runtime.
 *
 * @see com.sun.labs.util.props.Configurable
 * @see com.sun.labs.util.props.PropertySheet
 */
public class ConfigurationManager implements Cloneable {

    private List<ConfigurationChangeListener> changeListeners =
            new ArrayList<>();

    private Map<String, PropertySheet> symbolTable =
            new LinkedHashMap<>();

    private Map<String, RawPropertyData> rawPropertyMap =
            new LinkedHashMap<>();
    
    private Map<Component,PropertySheet> configuredComponents =
            new LinkedHashMap<>();

    private Map<String,PropertySheet> addedComponents =
            new LinkedHashMap<>();

    private GlobalProperties globalProperties = new GlobalProperties();
    
    private Map<String,SerializedObject> serializedObjects = new HashMap<>();
    
    private Map<String,Object> deserializedObjects = new HashMap<>();

    private GlobalProperties origGlobal;

    protected boolean showCreations;

    private List<URL> configURLs = new ArrayList<URL>();

    private ComponentRegistry registry;
    
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());

    private MBeanServer mbs;

    /**
     * Creates a new empty configuration manager. This constructor is only of use in cases when a system configuration
     * is created during runtime.
     */
    public ConfigurationManager() {

        // we can't config the configuration manager with itself so we
        // do some of these config items manually.
        origGlobal = new GlobalProperties();
        ConfigurationManagerUtils.applySystemProperties(rawPropertyMap,
                                                        globalProperties);
        GlobalProperty scGlobal = globalProperties.get("showCreations");
        if(scGlobal != null) {
            showCreations = "true".equals(scGlobal.getValue());
        }
    }

    /**
     * Creates a new configuration manager. Initial properties are loaded from the given URL. No need to keep the notion
     * of 'context' around anymore we will just pass around this property manager.
     *
     * @param url place to load initial properties from
     * @throws java.io.IOException if an error occurs while loading properties from the URL
     */
    public ConfigurationManager(URL url) throws IOException,
            PropertyException {
        
        configURLs.add(url);
        SaxLoader saxLoader = new SaxLoader(url, globalProperties);
        origGlobal = new GlobalProperties(globalProperties);
        rawPropertyMap = saxLoader.load();
        for(Map.Entry<String,SerializedObject> e : saxLoader.getSerializedObjects().entrySet()) {
            e.getValue().setConfigurationManager(this);
            serializedObjects.put(e.getKey(), e.getValue());
        }
        serializedObjects = saxLoader.getSerializedObjects();

        ConfigurationManagerUtils.applySystemProperties(rawPropertyMap,
                                                        globalProperties);

        // we can't config the configuration manager with itself so we
        // do some of these config items manually.
        GlobalProperty sC = globalProperties.get("showCreations");
        if(sC != null) {
            this.showCreations = "true".equals(sC.getValue());
        }

        //
        // Look up our distinguished registry name.
        setUpRegistry();
    }

    /**
     * Adds a set of properties at the given URL to the current configuration
     * manager.
     */
    public void addProperties(URL url) throws IOException, PropertyException {
        configURLs.add(url);

        //
        // We'll make local global properties and raw property data containers
        // so that we can manage the merge ourselves.
        GlobalProperties tgp = new GlobalProperties();
        SaxLoader saxLoader = new SaxLoader(url, tgp, rawPropertyMap);
        Map<String, RawPropertyData> trpm = saxLoader.load();
        for(Map.Entry<String,SerializedObject> e : saxLoader.getSerializedObjects().entrySet()) {
            e.getValue().setConfigurationManager(this);
            serializedObjects.put(e.getKey(), e.getValue());
        }

        //
        // Now, add the new global properties to the set for this configuration
        // manager, overriding as necessary.  Then do the same thing for the raw
        // property data.
        for(Map.Entry<String, GlobalProperty> e : tgp.entrySet()) {
            GlobalProperty op = globalProperties.put(e.getKey(), e.getValue());
            origGlobal.put(e.getKey(), e.getValue());
        }
        for(Map.Entry<String, RawPropertyData> e : trpm.entrySet()) {
            RawPropertyData opd = rawPropertyMap.put(e.getKey(), e.getValue());
        }
        
        ConfigurationManagerUtils.applySystemProperties(trpm, tgp);

        if(registry == null) {
            setUpRegistry();
        }
    }

    /**
     * Gets the current MBean server, creating one if necessary.
     * @return the current MBean server, or <code>null</code> if there isn't
     * one available.
     */
    protected MBeanServer getMBeanServer() {
        if(mbs == null) {
            mbs = ManagementFactory.getPlatformMBeanServer();
        }
        return mbs;
    }
    
    /**
     * Gets an input stream for a given location. We can use the stream
     * to deserialize objects that are part of our configuration.
     * <P>
     * We'll try to use the location as a resource, and failing that a URL, and
     * failing that, a file.
     * @param location the location provided.
     * @return an input stream for that location, or null if we couldn't find
     * any.
     */
    public InputStream getInputStreamForLocation(String location) {
        //
        // First, see if it's a resource on our classpath.
        InputStream ret = this.getClass().getResourceAsStream(location);
        if (ret == null) {
            try {
                //
                // Nope. See if it's a valid URL and open that.
                URL sfu = new URL(location);
                ret = sfu.openStream();
            } catch (MalformedURLException ex) {
                try {
                    //
                    // Not a valid URL, so try it as a file name.
                    ret = new FileInputStream(location);
                } catch (FileNotFoundException ex1) {
                    //
                    // Couldn't open the file, we're done.
                    return null;
                }
            } catch (IOException ex) {
                //
                // No joy.
                logger.warning("Cannot open serialized form " + location);
                return null;
            }
        }
        return ret;
    }


    
    /**
     * Makes sure that if a component registry is defined it is instantiated and
     * configured before anything else is looked up.
     */
    private void setUpRegistry() {
        PropertySheet ps = getPropertySheet("registry");
        if(ps == null) {
            return;
        }
        if(ps.getOwnerClass().getCanonicalName().equals("com.sun.labs.util.props.ComponentRegistry")) {
            registry = (ComponentRegistry) lookup("registry");
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
    public Remote getRemote(Remote r, Component c) {
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
    protected boolean wasLookedUp(Component c) {
        if(registry == null) {
            return false;
        }
        return registry.wasLookedUp(c);
    }
    
    /**
     * Shuts down the configuration manager, which just makes sure that any 
     * component registry that we may have is shut down.
     */
    public synchronized void shutdown() {
        if(registry != null) {
            registry.shutdown();
            registry = null;
        }
    }
    
    /**
     * Gets the raw properties associated with a given instance.
     * @param instanceName the name of the instance whose properties we want
     * @return the associated raw property data, or null if there is no data
     * associated with the given instance name.
     */
    public RawPropertyData getRawProperties(String instanceName) {
        return rawPropertyMap.get(instanceName);
    }

    /**
     * Returns the property sheet for the given object instance
     *
     * @param instanceName the instance name of the object
     * @return the property sheet for the object.
     */
    public PropertySheet getPropertySheet(String instanceName) {
        if(!symbolTable.containsKey(instanceName)) {
            // if it is not in the symbol table, so construct
            // it based upon our raw property data
            RawPropertyData rpd = rawPropertyMap.get(instanceName);
            if(rpd != null) {
                String className = rpd.getClassName();
                try {
                    Class cls = Class.forName(className);

                    // now load the property-sheet by using the class annotation
                    PropertySheet propertySheet =
                            new PropertySheet(cls,
                                              instanceName, this, rpd);

                    symbolTable.put(instanceName, propertySheet);

                } catch(ClassNotFoundException e) {
                    throw new PropertyException(e);
                } catch(ClassCastException e) {
                    throw new PropertyException(instanceName, "",
                                                "Unable to cast " + className +
                                                " to com.sun.labs.util.props.Configurable");
                }
            }
        }

        return symbolTable.get(instanceName);
    }

    /**
     * Gets all instances that are of the given type.
     *
     * @param type the desired type of instance
     * @return the set of all instances
     */
    public Collection<String> getInstanceNames(Class<? extends Configurable> type) {
        Collection<String> instanceNames = new ArrayList<String>();

        for(PropertySheet ps : symbolTable.values()) {
            if(!ps.isInstantiated()) {
                continue;
            }

            if(ConfigurationManagerUtils.isImplementingInterface(ps.getClass(),
                                                                 type)) {
                instanceNames.add(ps.getInstanceName());
            }
        }

        return instanceNames;
    }

    /**
     * Returns all names of configurables registered to this instance. The resulting set includes instantiated and
     * uninstantiated components.
     *
     * @return all component named registered to this instance of <code>ConfigurationManager</code>
     */
    public Collection<String> getComponentNames() {
        return new ArrayList<>(rawPropertyMap.keySet());
    }

    /**
     * Looks up an object that has been specified in the configuration
     * as one that was serialized. Note that such an object does not need
     * to be a component.
     * @param objectName the name of the object to lookup.
     * @return the deserialized object, or <code>null</code> if the object 
     * with that name does not occur in the configuration.
     */
    public Object lookupSerializedObject(String objectName) {
        SerializedObject so = serializedObjects.get(objectName);
        if(so == null) {
            return null;
        }
        return so.getObject();
    }
    /**
     * Looks up a configurable component by its name, instantiating it if
     * necessary.
     *
     * @param instanceName the name of the component that we want.
     * @return the instantiated component, or <code>null</code> if no such named
     * component exists in this configuration.
     * @throws InternalConfigurationException if there is some error instantiating the
     * component.
     */
    public Component lookup(String instanceName)
            throws InternalConfigurationException {
        return lookup(instanceName, null, true);
    }
    
    /**
     * Looks up a configurable component by its name, instantiating it if
     * necessary.
     *
     * @param instanceName the name of the component that we want.
     * @return the instantiated component, or <code>null</code> if no such named
     * component exists in this configuration.
     * @throws InternalConfigurationException if there is some error instantiating the
     * component.
     */
    public Component lookup(String instanceName, boolean reuseComponent)
            throws InternalConfigurationException {
        return lookup(instanceName, null, reuseComponent);
    }
    
    /**
     * Looks up a configurable component by name. If a component registry exists in the 
     * current configuration manager, it will be checked for the given component name.
     * If the component does not exist, it will be created.
     *
     * @param instanceName the name of the component
     * @param cl a listener for this component that is notified when components
     * are added or removed
     * @return the component, or null if a component was not found.
     * @throws InternalConfigurationException If the requested object could not be properly created, or is not a
     *                                        configurable object, or if an error occured while setting a component
     *                                        property.
     */
    public Component lookup(String instanceName, ComponentListener cl)
            throws InternalConfigurationException {
        return lookup(instanceName, null, true);
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
     *                                        configurable object, or if an error occured while setting a component
     *                                        property.
     */
    public Component lookup(String instanceName, ComponentListener cl, boolean reuseComponent)
            throws InternalConfigurationException {
        // apply all new propeties to the model
        instanceName = getStrippedComponentName(instanceName);
        Component ret = null;
        
        //
        // Get the property sheet for this component.
        PropertySheet ps = getPropertySheet(instanceName);
        
        if(ps == null) {
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
        if(registry != null && !ps.isExportable() &&
                ((ps.size() == 0 && ps.implementsRemote()) || ps.isImportable())) {
            ret = registry.lookup(ps, cl);
        }

        //
        // Need we look farther?
        if(ret == null) {


            if(logger.isLoggable(Level.FINER)) {
                logger.finer(String.format("lookup: %s", instanceName));
            }

            ret = ps.getOwner(ps, reuseComponent);
            
            if(ret instanceof Startable) {
                Startable stret = (Startable) ret;
                Thread t = new Thread(stret);
                t.setName(instanceName + "_thread");
                stret.setThread(t);
                t.start();
            }

            //
            // Do we need to export this?
            if(ps.isExportable() && registry != null) {
                registry.register(ret, ps);
            }

            //
            // Remember that we configured this component, removing it from the
            // list of added components if necessary.
            configuredComponents.put(ret, ps);
            addedComponents.remove(instanceName);
        }

        return ret;
    }

    /**
     * Gets the number of configured (i.e., instantiated components)
     *
     * @return the number of instantiated components in this configuration manager.
     */
    public int getNumConfigured() {
       return configuredComponents.size();
    }

    /**
     * Looks up a component by class.  Any component defined in the configuration
     * file may be returned.
     *
     * @param c the class that we want
     * @param cl a listener for things of this type
     * @return a component of the given type, or <code>null</code> if there are
     * no components of the given type.
     */

    public Component lookup(Class c, ComponentListener cl) {
        List<Component> comps = lookupAll(c, cl);
        if(comps.isEmpty()) {
            return null;
        }
        Collections.shuffle(comps);
        return comps.get(0);
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @param cl a listener that will report when components of the given type
     * are added or removed
     * @return a list of all the components with the given class name as their type.
     */
    public List<Component> lookupAll(Class c, ComponentListener cl) {

        List<Component> ret = new ArrayList<Component>();

        //
        // If the class isn't an interface, then lookup each of the names
        // in the raw property data with the given class
        // name, ignoring those things marked as importable.
        if(!c.isInterface()) {
            String className = c.getName();
            for (Map.Entry<String, RawPropertyData> e : rawPropertyMap.entrySet()) {
                if (e.getValue().getClassName().equals(className) &&
                        !e.getValue().isImportable()) {
                    ret.add(lookup(e.getKey()));
                }
            }
        }
        
        //
        // If we have a registry, then do a lookup for all things of the
        // given type.
        if(registry != null) {
            Component[] reg = registry.lookup(c, Integer.MAX_VALUE, cl);
            ret.addAll(Arrays.asList(reg));
        }
        return ret;
    }
    
    /**
     * Gets a list of all of the component names of the components that have 
     * a given type.  This will not instantiate the components.
     * 
     * @param c the class of the components that we want to look up.
     */
    public List<String> listAll(Class c) {
        List<String> ret = new ArrayList<String>();
        for(Map.Entry<String, RawPropertyData> e : rawPropertyMap.entrySet()) {
            RawPropertyData rpd = e.getValue();
            try {
                Class pclass = Class.forName(rpd.getClassName());
                try {
                    pclass.asSubclass(c);
                    ret.add(e.getKey());
                } catch(ClassCastException ex) {
                    continue;
                }
            } catch(ClassNotFoundException ex) {
                logger.warning(String.format("Non class %s found in config",
                                             rpd.getClassName()));
            }
        }

        return ret;
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
     * Reconfigures a component.  This is useful when, for example, a remote
     * service has thrown an exception and a component wishes to be reconfigured
     * in order to get a new instance of the required service.
     * 
     * @param c the component that needs reconfiguration
     */
    public void reconfigure(Component c) {
        PropertySheet ps = configuredComponents.get(c);
        if(ps != null && c instanceof Configurable) {
            ((Configurable) c).newProperties(ps);
        }
    }

    /**
     * Adds a component that was configured with a given property sheet to this
     * configuration manager.
     * @param c the configurable component
     * @param ps the property sheet containing the configuration for the component.
     */
    protected void addConfigured(Component c, PropertySheet ps) {
        configuredComponents.put(c, ps);
    }

    /**
     * Given a <code>Configurable</code>-class/interface, all property-sheets which are subclassing/implemting this
     * class/interface are collected and returned.  No <code>Configurable</code> will be instantiated by this method.
     */
    public List<PropertySheet> getPropSheets(Class<? extends Component> confClass) {
        List<PropertySheet> psCol = new ArrayList<PropertySheet>();

        for(PropertySheet ps : symbolTable.values()) {
            if(ConfigurationManagerUtils.isDerivedClass(ps.getConfigurableClass(),
                                                        confClass)) {
                psCol.add(ps);
            }
        }

        return psCol;
    }

    /**
     * Registers a new configurable to this configuration manager.
     *
     * @param confClass The class of the configurable to be instantiated and to be added to this configuration manager
     *                  instance.
     * @param name      The desired  lookup-name of the configurable
     * @param props     The properties to be used for component configuration
     * @throws IllegalArgumentException if the there's already a component with the same <code>name</code> that's been instantiated by
     *                                  this configuration manager instance.
     */
    public void addConfigurable(Class<? extends Configurable> confClass,
                                 String name, Map<String, Object> props) {
        if(name == null) {
            name = confClass.getName();
        }

        if(symbolTable.containsKey(name)) {
            throw new IllegalArgumentException("tried to override existing component name");
        }

        PropertySheet ps = getPropSheetInstanceFromClass(confClass, props, name,
                                                         this);
        symbolTable.put(name, ps);
        rawPropertyMap.put(name, new RawPropertyData(name, confClass.getName()));
        addedComponents.put(name, ps);
        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentAdded(this, ps);
        }
    }

    /**
     * Registers a new configurable to this configuration manager.
     *
     * @param confClass    The class of the configurable to be instantiated and to be added to this configuration
     *                     manager instance.
     * @param instanceName The desired  lookup-instanceName of the configurable
     * @throws IllegalArgumentException if the there's already a component with the same <code>instanceName</code>
     *                                  registered to this configuration manager instance.
     */
    public void addConfigurable(Class<? extends Configurable> confClass,
                                 String instanceName) {
        addConfigurable(confClass, instanceName, new HashMap<String, Object>());
    }

    /**
     * Renames a configurable component in this configuration manager.
     *
     * @param oldName the old name of the component
     * @param newName the new name of the component
     * @throws InternalConfigurationException if there is no component named
     * <code>oldName</code> in this configuration manager.
     */
    public void renameConfigurable(String oldName, String newName)
    throws InternalConfigurationException {
        PropertySheet ps = getPropertySheet(oldName);

        if(ps == null) {
            throw new InternalConfigurationException(oldName, null,
                    String.format("No configurable named %s to rename to %s",
                    oldName, newName));
        }

        ConfigurationManagerUtils.renameComponent(this, oldName, newName);

        symbolTable.remove(oldName);
        symbolTable.put(newName, ps);

        RawPropertyData rpd = rawPropertyMap.remove(oldName);
        rawPropertyMap.put(newName, new RawPropertyData(newName,
                                                        rpd.getClassName(),
                                                        rpd.getProperties()));

        fireRenamedConfigurable(oldName, newName);
    }

    /**
     * Removes a configurable from this configuration manager.
     * @param name the name of the configurable to remove
     * @return the property sheet associated with the component, or <code>null</code>
     * if no such component exists.
     */
    public PropertySheet removeConfigurable(String name) {

        PropertySheet ps = getPropertySheet(name);
        if(ps == null) {
            return null;
        }

        symbolTable.remove(name);
        rawPropertyMap.remove(name);
        addedComponents.remove(name); 
    
        //
        // If this one's been configured, remove it from there too!
        if(ps.isInstantiated()) {
            configuredComponents.remove(ps.getOwner());
        }

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentRemoved(this, ps);
        }
        return ps;
    }

    public void addSubConfiguration(ConfigurationManager subCM) {
        addSubConfiguration(subCM, false);
    }
    public void addSubConfiguration(ConfigurationManager subCM, boolean overwrite) {
        Collection<String> compNames = getComponentNames();

        if(!overwrite) {
            for(String addCompName : subCM.getComponentNames()) {
                if(compNames.contains(addCompName)) {
                    throw new RuntimeException(addCompName
                            + " is already registered to system configuration");
                }
            }
            for(String globProp : subCM.globalProperties.keySet()) {
                if(globalProperties.keySet().contains(globProp)) {
                    throw new IllegalArgumentException(globProp
                            + " is already registered as global property");
                }
            }
        }

        globalProperties.putAll(subCM.globalProperties);
        for(PropertySheet ps : subCM.symbolTable.values()) {
            ps.setCM(this);
        }

        symbolTable.putAll(subCM.symbolTable);
        rawPropertyMap.putAll(subCM.rawPropertyMap);
        
        RawPropertyData rpd = rawPropertyMap.get("search_engine");
    }

    /** Returns a copy of the map of global properties set for this configuration manager. */
    public GlobalProperties getGlobalProperties() {
        return new GlobalProperties(globalProperties);
    }

    /**
     * Returns a global property.
     *
     * @param propertyName The name of the global property or <code>null</code> if no such property exists
     */
    public String getGlobalProperty(String propertyName) {
        //        propertyName = propertyName.startsWith("$") ? propertyName : "${" + propertyName + "}";
        GlobalProperty globProp = globalProperties.get(propertyName);
        if(globProp == null) {
            return null;
        }
        return globalProperties.replaceGlobalProperties("_global", propertyName, globProp.toString());
    }

    public GlobalProperty getGloPropReference(String propertyName) {
        return globalProperties.get(propertyName);
    }

    /**
     * Returns the url of the xml-configuration which defined this configuration or <code>null</code>  if it was created
     * dynamically.
     */
    public List<URL> getConfigURLs() {
        return configURLs;
    }

    /**
     * Sets a global property.
     *
     * @param propertyName The name of the global property.
     * @param value        The new value of the global property. If the value is <code>null</code> the property becomes
     *                     removed.
     */
    public void setGlobalProperty(String propertyName, String value) {
        if(value == null) {
            globalProperties.remove(propertyName);
            origGlobal.remove(propertyName);
        } else {
            globalProperties.setValue(propertyName, value);
            origGlobal.setValue(propertyName, value);
        }

        // update all component configurations because they might be affected by the change
        for(String instanceName : getInstanceNames(Configurable.class)) {
            PropertySheet ps = getPropertySheet(instanceName);
            if(ps.isInstantiated()) {
                try {
                    Component comp = ps.getOwner();
                    if(comp instanceof Configurable) {
                        ((Configurable) comp).newProperties(ps);
                    }
                } catch(PropertyException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getStrippedComponentName(String propertyName) {
        assert propertyName != null;

        while(propertyName.startsWith("$")) {
            propertyName = globalProperties.get(GlobalProperty.stripGlobalSymbol(propertyName)).
                    toString();
        }

        return propertyName;
    }

    /** Adds a new listener for configuration change events. */
    public void addConfigurationChangeListener(ConfigurationChangeListener l) {
        if(l == null) {
            return;
        }

        changeListeners.add(l);
    }

    /** Removes a listener for configuration change events. */
    public void removeConfigurationChangeListener(ConfigurationChangeListener l) {
        if(l == null) {
            return;
        }

        changeListeners.remove(l);
    }

    /**
     * Informs all registered <code>ConfigurationChangeListener</code>s about a configuration changes the component
     * named <code>configurableName</code>.
     */
    void fireConfChanged(String configurableName, String propertyName) {
        assert getComponentNames().contains(configurableName);

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.configurationChanged(configurableName, propertyName,
                                                this);
        }
    }

    /**
     * Informs all registered <code>ConfigurationChangeListener</code>s about the component previously namesd
     * <code>oldName</code>
     */
    void fireRenamedConfigurable(String oldName, String newName) {
        assert getComponentNames().contains(newName);

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentRenamed(this, getPropertySheet(newName),
                                            oldName);
        }
    }

    /**
     * Test wether the given configuration manager instance equals this instance in terms of same configuration. This
     * This equals implemenation does not care about instantiation of components.
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof ConfigurationManager)) {
            return false;
        }

        ConfigurationManager cm = (ConfigurationManager) obj;

        Collection<String> setA = new HashSet<String>(getComponentNames());
        Collection<String> setB = new HashSet<String>(cm.getComponentNames());
        if(!setA.equals(setB)) {
            return false;
        }

        // make sure that all components are the same
        for(String instanceName : getComponentNames()) {
            PropertySheet myPropSheet = getPropertySheet(instanceName);
            PropertySheet otherPropSheet = cm.getPropertySheet(instanceName);

            if(!otherPropSheet.equals(myPropSheet)) {
                return false;
            }
        }

        // make sure that both configuration managers have the same set of global properties
        return cm.getGlobalProperties().equals(getGlobalProperties());
    }

    /** Creates a deep copy of the given CM instance. */
    // This is not tested yet !!!
    public Object clone() throws CloneNotSupportedException {
        ConfigurationManager cloneCM = (ConfigurationManager) super.clone();

        cloneCM.changeListeners = new ArrayList<ConfigurationChangeListener>();
        cloneCM.symbolTable = new LinkedHashMap<String, PropertySheet>();
        for(String compName : symbolTable.keySet()) {
            cloneCM.symbolTable.put(compName, (PropertySheet) symbolTable.get(compName).
                                    clone());
        }

        cloneCM.globalProperties = new GlobalProperties(globalProperties);
        cloneCM.rawPropertyMap =
                new HashMap<String, RawPropertyData>(rawPropertyMap);


        return cloneCM;
    }

    /**
     * Creates an instance of the given <code>Configurable</code> by using the default parameters as defined by the
     * class annotations to parameterize the component.
     */
    public static Component getInstance(Class<? extends Component> targetClass)
            throws PropertyException {
        return getInstance(targetClass, new HashMap<String, Object>());
    }

    /**
     * Creates an instance of the given <code>Configurable</code> by using the default parameters as defined by the
     * class annotations to parameterize the component. Default prarmeters will be overrided if a their names are
     * containd in the given <code>props</code>-map
     */
    public static Component getInstance(Class<? extends Component> targetClass,
                                          Map<String, Object> props) throws PropertyException {

        if(ConfigurationManagerUtils.isDerivedClass(targetClass,
                                                    Configurable.class)) {
            PropertySheet ps =
                    getPropSheetInstanceFromClass(targetClass.asSubclass(Configurable.class), props,
                                                  null,
                                                  new ConfigurationManager());
            
            return ps.getOwner();
        } else {
            try {
                return targetClass.newInstance();
            } catch(InstantiationException ex) {
                throw new PropertyException(ex, "", "",
                                            "Unable to instantiate component " +
                                            targetClass);
            } catch(IllegalAccessException ex) {
                throw new PropertyException(ex, "", "",
                                            "Unable to instantiate component " +
                                            targetClass);
            }
        }
    }

    /**
     * Instantiates the given <code>targetClass</code> and instruments it using default properties or the properties
     * given by the <code>defaultProps</code>.
     */
    private static PropertySheet getPropSheetInstanceFromClass(Class<? extends Configurable> targetClass,
                                                                 Map<String, Object> defaultProps,
                                                                 String componentName,
                                                                 ConfigurationManager cm) {
        RawPropertyData rpd = new RawPropertyData(componentName,
                                                  targetClass.getName());

        for(String confName : defaultProps.keySet()) {
            Object property = defaultProps.get(confName);

            if(property instanceof Class) {
                property = ((Class) property).getName();
            }

            rpd.getProperties().put(confName, property);
        }

        return new PropertySheet(targetClass, componentName, cm, rpd);
    }

    /**
     * Saves the current configuration to the given file
     *
     * @param file
     *                place to save the configuration
     * @throws IOException
     *                 if an error occurs while writing to the file
     */
    public void save(File file) throws IOException {
        save(file, false);
    }
    /**
     * Saves the current configuration to the given file
     *
     * @param file
     *                place to save the configuration
     * @throws IOException
     *                 if an error occurs while writing to the file
     */
    public void save(File file, boolean writeAll) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter writer = new PrintWriter(fos);
        save(writer, writeAll);
        writer.close();
    }
    /**
     * Writes the current configuration to the given writer.  Only components
     * that have been instantiated or programatically added will be written.
     *
     * @param file
     *                place to save the configuration
     * @throws IOException
     *                 if an error occurs while writing to the file
     */
    public void save(PrintWriter writer) throws IOException {
        save(writer, false);
    }

    /**
     * Writes the configuration to the given writer.
     * 
     * @param writer the writer to write to
     * @param writeAll if <code>true</code> all components will be written, 
     * whether they were instantiated or not.  If <code>false</code>
     * then only those components that were instantiated or added programatically
     * will be written.
     * @throws IOException 
     */
    public void save(PrintWriter writer, boolean writeAll) throws IOException {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        writer.println("<!--    Configuration file--> \n\n");

        writer.println("<config>");

        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");

        for(String propName : origGlobal.keySet()) {
            String propVal = origGlobal.get(propName).toString();

            Matcher matcher = pattern.matcher(propName);
            propName = matcher.matches() ? matcher.group(1) : propName;

            writer.printf("\t<property name=\"%s\" value=\"%s\"/>\n", propName, propVal);
        }
        
        //
        // A copy of the raw property data that we can use to keep track of what's 
        // been written.
        Set<String> allNames = new HashSet<String>(rawPropertyMap.keySet());
        for(PropertySheet ps : configuredComponents.values()) {
            ps.save(writer);
            allNames.remove(ps.getInstanceName());
        }

        for(PropertySheet ps : addedComponents.values()) {
            ps.save(writer);
            allNames.remove(ps.getInstanceName());
        }
        
        //
        // If we're supposed to, write the rest of the stuff.
        if(writeAll) {
            for(String instanceName : allNames) {
                PropertySheet ps = getPropertySheet(instanceName);
                ps.save(writer);
            }
        }

        writer.println("</config>");
    }

    private Field getNamedField(Object o, String fieldName) {
        Field[] fields = o.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            if(field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Imports a configurable component by generating the property sheets
     * necessary to configure the provided component and puts them into
     * this configuration manager.  This is useful in situations where you have
     * a configurable component but you don't have the property sheet that
     * generated it (e.g., if it was sent over the network.)
     *
     * @param configurable the configurable component to import
     * @param name the unique name to use for the component. This name will be
     * used to prefix any embedded components.
     */
    public void importConfigurable(Configurable configurable,
                                   String name) throws PropertyException {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        Field[] fields = configurable.getClass().getFields();

        //
        // The name of the configuration property for an annotated variable in
        // the configurable class that we were given.
        String propertyName = null;
        try {
            for(Field field : fields) {
                Annotation[] annotations = field.getAnnotations();
                for(Annotation annotation : annotations) {
                    if(annotation instanceof ConfigComponent) {

                        propertyName = (String) field.get(null);

                        //
                        // A single component.
                        m.put(propertyName, importComponent(configurable, propertyName,
                                                              name));
                    } else if(annotation instanceof ConfigComponentList) {
                        propertyName = (String) field.get(null);

                        //
                        // A list of components.
                        m.put(propertyName, importComponentList(configurable, propertyName,
                                                                  name));
                    } else {
                        Annotation[] superAnnotations = annotation.
                                annotationType().
                                getAnnotations();
                        propertyName = (String) field.get(null);

                        //
                        // One of the other configuration types.
                        for(Annotation superAnnotation : superAnnotations) {
                            if(superAnnotation instanceof ConfigProperty) {
                                m.put(propertyName, importPropertyValue(configurable, propertyName));
                            }
                        }
                    }
                }
            }
            addConfigurable(configurable.getClass(), name, m);
        } catch(PropertyException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new PropertyException(ex, null, null,
                                        String.format("Error importing %s for propName",
                                        name, propertyName));
        }
    }

    /**
     * Imports a list of configurable components that is a property of another
     * configurable into this configuration manager.
     *
     * @param configurable the configurable that has the list of configurable
     * components as a property
     * @param propertyName the name of the property containing the list of
     * configurable components
     * @param prefix the prefix to use to name the components in the list
     * @return a list of the names assigned to the components in the list.
     * @throws PropertyException if the embedded list of components cannot be
     * retrieved or if one of the components in the list cannot be imported.
     */
    private List<String> importComponentList(Configurable configurable, 
            String propertyName, String prefix)
            throws PropertyException {

        try {
            //
            // Get the list of configurable components.
            Field field = getNamedField(configurable, propertyName);

            if(field == null) {
                throw new PropertyException(null, propertyName, String.format(
                        "Property %s has no variable named %s", propertyName,
                        propertyName));
            }

            List<Component> l = (List<Component>) field.get(configurable);
            List<String> names = new ArrayList<String>();
            String embeddedName = String.format("%s-%s", prefix, propertyName);
            int i = 0;
            for(Component c : l) {
                String np = String.format("%s-%d", embeddedName, i++);
                importConfigurable((Configurable) c, np);
                names.add(np);
            }
            return names;
        } catch(PropertyException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new PropertyException(ex, null, propertyName,
                                        String.format(
                    "Error getting component field"));
        }
    }

    /**
     * Imports a configurable component that is a property of another configurable
     * component.
     *
     * @param configurable the configurable component that has the configurable
     * component that we want to import as a property.
     * @param propertyName the name of the property of the provided configurable component
     * that contains the component to import
     * @param prefix a prefix to use to specify the component name of the
     * component that we'll be importing.
     * @return the name that we used for the component.
     * @throws PropertyException
     */
    private String importComponent(Configurable configurable,
                                   String propertyName, String prefix) throws
            PropertyException {
        try {
            String newComponentName = String.format("%s-%s", prefix,
                                                    propertyName);
            importComponentInternal(configurable, propertyName, newComponentName);
            return newComponentName;
        } catch(Exception ex) {
            throw new PropertyException(ex, null, propertyName,
                                        String.format(
                    "Error getting component field"));
        }
    }

    /**
     * Imports a configurable component that is a property of another configurable
     * component.
     *
     * @param configurable the configurable component that has the configurable
     * that we want to import as a property.
     * @param propertyName the name of the property that is associated with with
     * configurable component that we want to import.
     * @param componentName the name of the component to be used to uniquely
     * identify it in the configuration.
     */
    private void importComponentInternal(Configurable configurable,
                                         String propertyName,
                                         String componentName) throws PropertyException {

        try {

            //
            // Get the configurable that we actually want to import.
            Field field = getNamedField(configurable, propertyName);
            if(field == null) {
                throw new PropertyException(null, propertyName, String.format(
                        "Property %s has no variable named %s", propertyName,
                        propertyName));
            }
            Configurable newConf = (Configurable) field.get(configurable);
            importConfigurable(newConf, componentName);
        } catch(PropertyException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new PropertyException(ex, null, propertyName,
                                        String.format(
                    "Error importing component field for property %s",
                    propertyName));
        }
    }

    /**
     * Fetches the value that's associated with a given property name in the
     * given configurable component.  This method relies on the convention that
     * a given property name will be stored in the instance variable with that name.
     * If that is not the case, then a <code>PropertyException</code> will be
     * thrown
     *
     * @param configurable the configurable component from which we want to fetch
     * a property value.
     * @param propertyName The name of the property for the configurable component for
     * which we want to fetch a value.
     * @return the value for this property name in this configurable component
     * @throws PropertyException if there is not an appropriately named instance
     * variable, or if there is some other error accessing the value of the
     * instance variable.
     */
    private Object importPropertyValue(Configurable configurable, String propertyName) throws PropertyException {
        try {
            Field field = getNamedField(configurable, propertyName);
            if(field == null) {
                throw new PropertyException(null, propertyName, String.format(
                        "Property %s has no variable named %s", propertyName,
                        propertyName));
            }
            return field.get(configurable);
        } catch(PropertyException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new PropertyException(ex, null, propertyName,
                                        String.format(
                    "Error importing property field for property %s", propertyName));
        }
    }
}
