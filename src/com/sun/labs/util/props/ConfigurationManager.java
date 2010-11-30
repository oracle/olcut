package com.sun.labs.util.props;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            new ArrayList<ConfigurationChangeListener>();

    private Map<String, PropertySheet> symbolTable =
            new LinkedHashMap<String, PropertySheet>();

    private Map<String, RawPropertyData> rawPropertyMap =
            new LinkedHashMap<String, RawPropertyData>();
    
    private Map<Component,PropertySheet> configuredComponents =
            new LinkedHashMap<Component,PropertySheet>();

    private Map<String,PropertySheet> addedComponents =
            new LinkedHashMap<String,PropertySheet>();

    private GlobalProperties globalProperties = new GlobalProperties();

    private GlobalProperties origGlobal;

    protected boolean showCreations;

    private List<URL> configURLs = new ArrayList<URL>();

    private ComponentRegistry registry;
    
    private Logger logger = Logger.getLogger(getClass().getName());

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
        GlobalProperty showCreations = globalProperties.get("showCreations");
        if(showCreations != null) {
            this.showCreations = "true".equals(showCreations.getValue());
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

        //
        // Now, add the new global properties to the set for this configuration
        // manager, overriding as necessary.  Then do the same thing for the raw
        // property data.
        for(Map.Entry<String, GlobalProperty> e : tgp.entrySet()) {
            GlobalProperty op = globalProperties.put(e.getKey(), e.getValue());
            origGlobal.put(e.getKey(), e.getValue());
            if(op != null) {
            //                LogManager.getLogManager().getLogger("")
//                .warning("Overriding global property: " + e.getKey() +
//                        " old value: " + x + " new value: " + e.getValue());
            }
        }
        for(Map.Entry<String, RawPropertyData> e : trpm.entrySet()) {
            RawPropertyData opd = rawPropertyMap.put(e.getKey(), e.getValue());
            if(opd != null) {
            //                LogManager.getLogManager().getLogger("")
//                .warning("Overriding component: " + e.getKey() +
//                        " old value: " + x + " new value: " + e.getValue());
            }
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
     * Indeicates whether the given component was looked up in a service
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
//                    PropertySheet propertySheet = new PropertySheet(cls, this, rpd.flatten(globalProperties));
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
            if(!ps.isInstanciated()) {
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
     * noninstantiated components.
     *
     * @return all component named registered to this instance of <code>ConfigurationManager</code>
     */
    public Collection<String> getComponentNames() {
        return new ArrayList<String>(rawPropertyMap.keySet());
    }

    public Component lookup(String instanceName)
            throws InternalConfigurationException {
        return lookup(instanceName, null);
    }
    
    /**
     * Looks up a configurable component by name. If a component registry exists in the 
     * current configuration manager, it will be checked for the given component name.
     * If the component does not exist, it will be created.
     *
     * @param instanceName the name of the component
     * @return the component, or null if a component was not found.
     * @throws InternalConfigurationException If the requested object could not be properly created, or is not a
     *                                        configurable object, or if an error occured while setting a component
     *                                        property.
     */
    public Component lookup(String instanceName, ComponentListener cl)
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


            //
            // Instantiate the component (this may return null if our registry
            // is screwed up!)
            if(ps == null) {
                return null;
            }

            if(logger.isLoggable(Level.FINER)) {
                logger.finer(String.format("lookup: %s", instanceName));
            }

            ret = ps.getOwner(ps);
            
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
        if(comps.size() == 0) {
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
            for(Component m : reg) {
                ret.add(m);
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

    public void renameConfigurable(String oldName, String newName) {
        PropertySheet ps = getPropertySheet(oldName);

        if(ps == null) {
            throw new RuntimeException("no configurable (to be renamed) named " +
                                       oldName + " is contained in the CM");
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

    /** Removes a configurable from this configuration manager. */
    public void removeConfigurable(String name) {
        assert getComponentNames().contains(name);

        PropertySheet ps = symbolTable.remove(name);
        rawPropertyMap.remove(name);

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentRemoved(this, ps);
        }
    }

    public void addSubConfiguration(ConfigurationManager subCM) {
        Collection<String> compNames = getComponentNames();

        for(String addCompName : subCM.getComponentNames()) {
            if(compNames.contains(addCompName)) {
                throw new RuntimeException(addCompName +
                                           " is already registered to system configuration");
            }
        }

        for(String globProp : subCM.globalProperties.keySet()) {
            if(globalProperties.keySet().contains(globProp)) {
                throw new IllegalArgumentException(globProp +
                                                   " is already registered as global property");
            }
        }

        globalProperties.putAll(subCM.globalProperties);
        for(PropertySheet ps : subCM.symbolTable.values()) {
            ps.setCM(this);
        }

        symbolTable.putAll(subCM.symbolTable);
        rawPropertyMap.putAll(subCM.rawPropertyMap);
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
        String ret = globProp.toString();
        while(ret != null && GlobalProperty.isGlobalProperty(ret)) {
            globProp = globalProperties.get(GlobalProperty.getPropertyName(ret));
            if(globProp == null) {
                return null;
            }
            ret = globProp.toString();
        }
        return ret;
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
            if(ps.isInstanciated()) {
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
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter writer = new PrintWriter(fos);
        save(writer);
        writer.close();
    }

    /**
     * Saves the current configuration to the given file
     *
     * @param file
     *                place to save the configuration
     * @throws IOException
     *                 if an error occurs while writing to the file
     */
    public void save(PrintWriter writer) throws IOException {
        StringBuilder sb = new StringBuilder();
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

        for(PropertySheet ps : configuredComponents.values()) {
            ps.save(writer);
        }

        for(PropertySheet ps : addedComponents.values()) {
            ps.save(writer);
        }

        writer.println("</config>");
    }
}

