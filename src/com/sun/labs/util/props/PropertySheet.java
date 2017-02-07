package com.sun.labs.util.props;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import net.jini.core.lease.Lease;

/**
 * A property sheet which defines a collection of properties for a single
 * component in the system.
 *
 * @author Holger Brandl
 */
public class PropertySheet implements Cloneable {
    
    public enum PropertyType {

        INT, DOUBLE, BOOL, ENUM, COMP, STRING, STRINGLIST, COMPLIST, ENUMSET;

    }
    private Map<String, ConfigPropWrapper> registeredProperties
            = new HashMap<String, ConfigPropWrapper>();

    private Map<String, Object> propValues = new HashMap<String, Object>();

    private Pattern arraySplit = Pattern.compile(",\\s*");

    /**
     * Maps the names of the component properties to their (possibly unresolved)
     * values.
     * <p/>
     * Example: <code>frontend</code> to <code>${myFrontEnd}</code>
     */
    private Map<String, Object> rawProps = new HashMap<String, Object>();

    private Map<String, Object> flatProps;

    private ConfigurationManager cm;

    private Component owner;

    private RawPropertyData rpd;

    private boolean exportable;

    private boolean importable;

    private boolean implementsRemote;
    
    private String serializedForm;

    /**
     * The time to lease this object.
     */
    private long leaseTime = Lease.ANY;

    /**
     * The name of the component containing a list of configuration entries.
     */
    private String entriesName;

    /**
     * The configuration entries to use for service registration or matching.
     */
    private ConfigurationEntry[] entries;

    private final Class<? extends Component> ownerClass;

    private String instanceName;

    public static final String PROP_LOG_LEVEL = "logLevel";

    private Level logLevel;

    @SuppressWarnings("NonConstantLogger")
    private Logger logger = Logger.getLogger(getClass().getName());

    public PropertySheet(Configurable configurable, String name,
            RawPropertyData rpd, ConfigurationManager ConfigurationManager) {
        this(configurable.getClass(), name, ConfigurationManager, rpd);
        owner = configurable;
    }

    public PropertySheet(Class<? extends Configurable> confClass, String name,
            ConfigurationManager cm, RawPropertyData rpd) {
        ownerClass = confClass;
        this.cm = cm;
        this.instanceName = name;
        exportable = rpd.isExportable();
        importable = rpd.isImportable();
        leaseTime = rpd.getLeaseTime();
        entriesName = rpd.getEntriesName();
        serializedForm = rpd.getSerializedForm();

        //
        // Does this class implement remote?
        for (Class iface : ownerClass.getInterfaces()) {
            if (iface.equals(java.rmi.Remote.class)) {
                implementsRemote = true;
            }
        }

        processAnnotations(this, confClass);

        //
        // If there were any properties in the XML file that were not annotated,
        // then throw a property excepion.  Note that any component can specify the
        // log level without having it annotated!
        for (String propName : rpd.getProperties().keySet()) {
            if (!propValues.containsKey(propName)
                    && !propName.equals(PROP_LOG_LEVEL)) {
                throw new PropertyException(getInstanceName(), propName,
                        "Unknown property in configuration file.");
            }
        }

        //
        // If we're supposed to have configuration entries, then get them now.
        if (entriesName != null) {
            ConfigurationEntries ce
                    = (ConfigurationEntries) cm.lookup(entriesName);
            if (ce == null) {
                throw new PropertyException(getInstanceName(), "entries",
                        "Cannot find entries component " + entriesName);
            }
            entries = ce.getEntries();
        }

        // now apply all xml properties
        flatProps = rpd.flatten(cm).getProperties();
        rawProps = new HashMap<String, Object>(rpd.getProperties());

        for (String propName : rawProps.keySet()) {
            propValues.put(propName, flatProps.get(propName));
        }
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    public boolean isExportable() {
        return exportable;
    }

    public boolean implementsRemote() {
        return implementsRemote;
    }

    public void setImportable(boolean importable) {
        this.importable = importable;
    }

    public boolean isImportable() {
        return importable;
    }

    public void setLeaseTime(long leaseTime) {
        this.leaseTime = leaseTime;
    }

    public long getLeaseTime() {
        return leaseTime;
    }

    public ConfigurationEntry[] getEntries() {
        return entries;
    }

    public Collection<String> getPropertyNames() {
        //
        // Make sure the log level is in the set!
        Set<String> ps = new HashSet<String>(rawProps.keySet());
        ps.add("logLevel");
        return ps;
    }

    /**
     * Registers a new property which type and default value are defined by the
     * given sphinx property.
     *
     * @param propName The name of the property to be registered.
     * @param property The property annoation masked by a proxy.
     */
    private void registerProperty(String propName, ConfigPropWrapper property) {
        assert property != null && propName != null;

        registeredProperties.put(propName, property);
        propValues.put(propName, null);
        rawProps.put(propName, null);
    }

    /**
     * Returns the property names <code>name</code> which is still wrapped into
     * the annotation instance.
     */
    public ConfigPropWrapper getProperty(String name, Class propertyClass)
            throws PropertyException {
        if (!propValues.containsKey(name)) {
            throw new InternalConfigurationException(getInstanceName(), name,
                    "Unknown property '"
                    + name + "' ! Make sure that you've annotated it.");
        }

        ConfigPropWrapper s4PropWrapper = registeredProperties.get(name);

        try {
            propertyClass.cast(s4PropWrapper.getAnnotation());
        } catch (ClassCastException e) {
            throw new InternalConfigurationException(e, getInstanceName(), name, name
                    + " is not an annotated sphinx property of '" + getConfigurableClass().
                    getName() + "' !");
        }

        return s4PropWrapper;
    }

    /**
     * Gets the value associated with this name
     *
     * @param name the name
     * @return the value
     */
    public String getString(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigString.class);
        ConfigString s4String = ((ConfigString) s4PropWrapper.getAnnotation());

        if (propValues.get(name) == null) {
            boolean isDefDefined
                    = !s4String.defaultValue().equals(ConfigString.NOT_DEFINED);

            if (s4String.mandatory()) {
                if (!isDefDefined) {
                    throw new InternalConfigurationException(getInstanceName(),
                            name, "mandatory property is not set!");
                }
            }
            propValues.put(name, isDefDefined ? s4String.defaultValue() : null);
        }

        String propValue = flattenProp(name);

        //check range
        List<String> range = Arrays.asList(s4String.range());
        if (!range.isEmpty() && !range.contains(propValue)) {
            throw new InternalConfigurationException(getInstanceName(), name, " is not in range ("
                    + range + ")");
        }

        return propValue;
    }

    public List<String> getStringList(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name,
                ConfigStringList.class);
        ConfigStringList configStringList
                = ((ConfigStringList) s4PropWrapper.getAnnotation());
        List<String> vals = (List<String>) propValues.get(name);
        if (vals == null) {
            String[] dv = configStringList.defaultList();
            if (configStringList.mandatory() && dv == null) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        " requires list values");
            }
            return dv == null ? new ArrayList<String>() : Arrays.asList(dv);
        }

        //
        // Do global property replacement.
        List<String> temp = new ArrayList<String>();
        for (String s : vals) {
            temp.add(getConfigurationManager().getGlobalProperties().replaceGlobalProperties(getInstanceName(), name, s));
        }
        vals = temp;

        List<String> range = Arrays.asList(configStringList.range());
        for (String s : vals) {
            if (!range.isEmpty() && !range.contains(s)) {
                throw new InternalConfigurationException(getInstanceName(), name, " is not in range ("
                        + range + ")");
            }
        }
        return vals;
    }

    public Enum getEnum(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigEnum.class);
        ConfigEnum configEnum = ((ConfigEnum) s4PropWrapper.getAnnotation());
        Object val = propValues.get(name);

        if (val != null && val instanceof Enum) {
            return (Enum) val;
        }

        //
        // If we were given a string, then do some global replacement on it.
        if (val != null && val instanceof String) {
            val = flattenProp(name);
        }

        if (val == null) {
            if (!configEnum.defaultValue().equals("")) {
                val = configEnum.defaultValue();
            } else {
                throw new InternalConfigurationException(getInstanceName(),
                        name,
                        String.format("Default value %s is not valid for enum %s",
                                configEnum.defaultValue(), configEnum.type()));
            }
        }

        Enum ret;
        try {
            ret = Enum.valueOf(configEnum.type(), (String) val);
        } catch (Exception e) {
            try {
                ret = Enum.valueOf(configEnum.type(), ((String) val).toUpperCase());
            } catch (Exception e2) {
                throw new InternalConfigurationException(getInstanceName(),
                        name,
                        String.format("Value %s is not valid for enum %s",
                                configEnum.defaultValue(), configEnum.type()));
            }
        }
        propValues.put(name, ret);
        return ret;
    }

    public EnumSet getEnumSet(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigEnumSet.class);
        ConfigEnumSet configEnumSet = ((ConfigEnumSet) s4PropWrapper.getAnnotation());
        Object vals = propValues.get(name);

        if (vals != null && vals instanceof EnumSet) {
            return (EnumSet) vals;
        }

        Class enumClass = configEnumSet.type();
        EnumSet ret = EnumSet.noneOf(enumClass);
        String[] evs;

        if (vals == null) {
            evs = configEnumSet.defaultList();
            if (configEnumSet.mandatory() && evs == null) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        " requires enum values");
            }
        } else {
            evs = ((List<String>) vals).toArray(new String[0]);
        }

        //
        // Parse the values.
        if (evs != null) {
            for (String ev : evs) {

                try {
                    ret.add(Enum.valueOf(enumClass, ev.toUpperCase()));
                } catch (Exception e) {
                    throw new InternalConfigurationException(
                            getInstanceName(),
                            name,
                            String.format(
                                    "Value %s is not valid for enum %s",
                                    ev, enumClass.getName()));
                }
            }
        }
        propValues.put(name, ret);
        return ret;
    }

    public File getFile(String propName) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(propName, ConfigFile.class);
        ConfigFile configFile = ((ConfigFile) s4PropWrapper.getAnnotation());
        Object val = propValues.get(propName);

        //
        // Val will initially have the bare string from the config file.
        // We should change it into a File.
        if (val == null || val instanceof String) {
            boolean isDefined = !configFile.defaultValue().equals(ConfigString.NOT_DEFINED);

            if (configFile.mandatory()) {
                if (!isDefined) {
                    throw new InternalConfigurationException(getInstanceName(),
                            propName, "mandatory property is not set!");
                }
            }

            String fileName = flattenProp(propName);
            File f = null;
            if (fileName == null) {
                if (configFile.mandatory()) {
                    throw new PropertyException(instanceName, propName, "Must specify file name");
                }
            } else {
                f = new File(fileName);
                if (!f.exists()) {
                    if (configFile.exists()) {
                        throw new PropertyException(instanceName, propName, "File doesn't exist: " + f);
                    } else if (configFile.canRead()) {
                        getLogger();
                        logger.warning("canRead specified for file that doesn't exist: " + f);
                    } else if (configFile.canWrite()) {
                        File parent = f.getParentFile();
                        if (!parent.canWrite()) {
                            throw new PropertyException(instanceName, propName,
                                    "canWrite specified for file, but directory " + parent.getAbsolutePath() + " is not writeable");
                        }
                    }
                } else {
                    if (configFile.canRead() && !f.canRead()) {
                        throw new PropertyException(instanceName, propName, "Can't read file: " + f);
                    }
                    if (configFile.canWrite() && !f.canWrite()) {
                        throw new PropertyException(instanceName, propName, "Can't write file: " + f);
                    }
                    if (configFile.isDirectory() && !f.isDirectory()) {
                        throw new PropertyException(instanceName, propName, f + "is not a directory" + f);
                    }
                }
            }
            propValues.put(propName, f);
            return f;
        } else {
            return (File) val;
        }
    }

    private String flattenProp(String name) {
        Object value = propValues.get(name);
        if (value == null) {
            return null;
        }

        String ret = value.toString();
        return cm.getGlobalProperties().replaceGlobalProperties(getInstanceName(),
                name, ret);
    }

    /**
     * Gets the value associated with this name
     *
     * @param name the name
     * @return the value
     * @throws edu.cmu.sphinx.util.props.PropertyException if the named property
     * is not of this type
     */
    public int getInt(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigInteger.class);
        ConfigInteger s4Integer = (ConfigInteger) s4PropWrapper.getAnnotation();

        if (propValues.get(name) == null) {
            boolean isDefDefined = !(s4Integer.defaultValue()
                    == ConfigInteger.NOT_DEFINED);

            if (s4Integer.mandatory()) {
                if (!isDefDefined) {
                    throw new InternalConfigurationException(getInstanceName(),
                            name, "mandatory property is not set!");
                }
            } else if (!isDefDefined) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        "no default value for non-mandatory property");
            }

            propValues.put(name, s4Integer.defaultValue());
        }
        Object propObject = propValues.get(name);
        try {
            Integer propValue = propObject instanceof Integer
                    ? (Integer) propObject
                    : Integer.decode(flattenProp(name));

            int[] range = s4Integer.range();
            if (range.length != 2) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        range
                        + " is not of expected range type, which is {minValue, maxValue)");
            }
            if (propValue < range[0] || propValue > range[1]) {
                throw new InternalConfigurationException(getInstanceName(), name, " is not in range ("
                        + range + ")");
            }
            return propValue;
        } catch (NumberFormatException ex) {
            throw new PropertyException(instanceName, name, String.format("%s is not an integer", propObject));
        }

    }

    /**
     * Gets the value associated with this name
     *
     * @param name the name
     * @return the value
     * @throws edu.cmu.sphinx.util.props.PropertyException if the named property
     * is not of this type
     */
    public float getFloat(String name) throws PropertyException {
        return ((Double) getDouble(name)).floatValue();
    }

    /**
     * Gets the value associated with this name
     *
     * @param name the name
     * @return the value
     * @throws edu.cmu.sphinx.util.props.PropertyException if the named property
     * is not of this type
     */
    public double getDouble(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigDouble.class);
        ConfigDouble s4Double = (ConfigDouble) s4PropWrapper.getAnnotation();

        if (propValues.get(name) == null) {
            boolean isDefDefined = !(s4Double.defaultValue()
                    == ConfigDouble.NOT_DEFINED);

            if (s4Double.mandatory()) {
                if (!isDefDefined) {
                    throw new InternalConfigurationException(getInstanceName(),
                            name, "mandatory property is not set!");
                }
            } else if (!isDefDefined) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        "no default value for non-mandatory property");
            }

            propValues.put(name, s4Double.defaultValue());
        }
        Object propObject = propValues.get(name);
        try {
            Double propValue
                    = propObject instanceof Double ? (Double) propObject : Double.
                            valueOf(flattenProp(name));

            double[] range = s4Double.range();
            if (range.length != 2) {
                throw new InternalConfigurationException(getInstanceName(), name,
                        range
                        + " is not of expected range type, which is {minValue, maxValue)");
            }
            if (propValue < range[0] || propValue > range[1]) {
                throw new InternalConfigurationException(getInstanceName(), name, " is not in range ("
                        + range + ")");
            }
            return propValue;
        } catch (NumberFormatException ex) {
            throw new PropertyException(instanceName, name, String.format(
                    "%s is not a double", propObject));
        }
    }

    /**
     * Gets the value associated with this name
     *
     * @param name the name
     * @return the value
     * @throws edu.cmu.sphinx.util.props.PropertyException if the named property
     * is not of this type
     */
    public Boolean getBoolean(String name) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name, ConfigBoolean.class);
        ConfigBoolean s4Boolean = (ConfigBoolean) s4PropWrapper.getAnnotation();

        if (propValues.get(name) == null && !s4Boolean.isNotDefined()) {
            propValues.put(name, s4Boolean.defaultValue());
        }

        Object propValue = propValues.get(name);
        if (propValue instanceof String) {
            propValue = Boolean.valueOf((String) propValue);
        }

        return (Boolean) propValue;
    }

    /**
     * Gets a component associated with the given parameter name
     *
     * @param name the parameter name
     * @return the component associated with the name
     * @throws edu.cmu.sphinx.util.props.PropertyException if the component does
     * not exist or is of the wrong type.
     */
    public Component getComponent(String name) throws PropertyException {
        return getComponent(name, null);
    }

    /**
     * Gets a component by type.
     *
     * @param c the class of the component that we want to look up
     * @param cl a listener for new components of this type.
     * @return a component of the given type, or <code>null</code> if there is
     * no component of the given type.
     */
    public Component getComponent(Class c, ComponentListener cl) {
        return cm.lookup(c, cl);
    }

    public Component getComponent(String name, ComponentListener cl) throws PropertyException {
        ConfigPropWrapper s4PropWrapper = getProperty(name,
                ConfigComponent.class);

        ConfigComponent s4Component = (ConfigComponent) s4PropWrapper.getAnnotation();
        Class expectedType = s4Component.type();

        Object propVal = propValues.get(name);

        if (propVal != null && propVal instanceof Component) {
            //
            // If we looked this up in a registry, we probably want to re-look 
            // it up, so we'll set the property value to null and put the original
            // raw property back in the map.
            if (getConfigurationManager().wasLookedUp((Component) propVal)) {
                propVal = null;
                propValues.put(name, flatProps.get(name));
                PropertySheet ps = cm.getPropertySheet(flattenProp(name));
                ps.clearOwner();
            }
        }
        if (propVal == null || propVal instanceof String
                || propVal instanceof GlobalProperty) {
            Component configurable = null;
            PropertySheet ps = null;

            try {
                if (propValues.get(name) != null) {
                    ps = cm.getPropertySheet(flattenProp(name));
                    if (ps != null) {
                        configurable = ps.getOwner(this, cl);
                    }
                }

                if (configurable != null
                        && !expectedType.isInstance(configurable)) {
                    throw new InternalConfigurationException(getInstanceName(),
                            name,
                            "mismatch between annoation and component type");
                }

                if (configurable == null) {
                    Class<? extends Component> defClass;

                    if (propValues.get(name) != null) {
                        defClass
                                = (Class<? extends Component>) Class.forName((String) propValues.get(name));
                    } else {
                        defClass = s4Component.defaultClass();
                    }

                    if (defClass.equals(Component.class)
                            && s4Component.mandatory()) {
                        throw new InternalConfigurationException(getInstanceName(),
                                name, "mandatory property is not set!");

                    } else {
                        if (Modifier.isAbstract(defClass.getModifiers())
                                && s4Component.mandatory()) {
                            throw new InternalConfigurationException(getInstanceName(),
                                    name, defClass.getName() + " is abstract!");
                        }

                        // because we're forced to use the default type, assert that it is set
                        if (defClass.equals(Component.class)) {
                            if (s4Component.mandatory()) {
                                throw new InternalConfigurationException(getInstanceName(),
                                        name, instanceName
                                        + ": no default class defined for " + name);
                            } else {
                                return null;
                            }
                        }

                        configurable = ConfigurationManager.getInstance(defClass);
                        assert configurable != null;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new PropertyException(e);
            }

            propValues.put(name, configurable);
            cm.addConfigured(configurable, ps);
        }
        return (Component) propValues.get(name);
    }

    /**
     * Returns the class of of a registered component property without
     * instantiating it.
     */
    public Class<? extends Component> getComponentClass(String propName) {
        Class<? extends Component> defClass = null;

        if (propValues.get(propName) != null) {
            try {
                defClass
                        = (Class<? extends Component>) Class.forName((String) propValues.get(propName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            ConfigComponent comAnno = (ConfigComponent) registeredProperties.get(propName).
                    getAnnotation();
            defClass = comAnno.defaultClass();
            if (comAnno.mandatory()) {
                defClass = null;
            }
        }

        return defClass;
    }

    /**
     * Gets a list of components associated with the given parameter name
     *
     * @param name the parameter name
     * @return the component associated with the name
     * @throws edu.cmu.sphinx.util.props.PropertyException if the component does
     * not exist or is of the wrong type.
     */
    public List<? extends Component> getComponentList(String name) throws InternalConfigurationException {
        return getComponentList(name, null);
    }

    public List<? extends Component> getComponentList(String name, ComponentListener cl) throws InternalConfigurationException {
        getProperty(name, ConfigComponentList.class);

        List components = (List) propValues.get(name);

        assert registeredProperties.get(name).getAnnotation() instanceof ConfigComponentList;
        ConfigComponentList annotation = (ConfigComponentList) registeredProperties.get(name).
                getAnnotation();

        // no componets names are available and no comp-list was yet loaded
        // therefore load the default list of components from the annoation
        if (components == null) {
            List<Class<? extends Component>> defClasses
                    = Arrays.asList(annotation.defaultList());

            //            if (annoation.mandatory() && defClasses.isEmpty())
//                throw new InternalConfigurationException(getInstanceName(), name, "mandatory property is not set!");
            components = new ArrayList<Component>();

            for (Class<? extends Component> defClass : defClasses) {
                components.add(ConfigurationManager.getInstance(defClass));
            }

            propValues.put(name, components);
        }

        if (!components.isEmpty() && !(components.get(0) instanceof Component)) {

            List<Component> list = new ArrayList<Component>();

            for (Object component : components) {

                if (component instanceof String) {
                    Component configurable = cm.lookup((String) component, cl);

                    //
                    // If we couldn't find this component, then throw a property exception.
                    if (configurable == null) {
                        throw new PropertyException(instanceName, name,
                                "Unknown component " + component
                                + " in list: " + name);
                    }
                    list.add(configurable);
                } else if (component instanceof Class) {
                    list.addAll(cm.lookupAll((Class) component, cl));
                }
            }

            propValues.put(name, list);
        }
        return (List<? extends Configurable>) propValues.get(name);
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String newInstanceName) {
        this.instanceName = newInstanceName;
    }

    /**
     * Returns true if the owner of this property sheet is already instantiated.
     */
    public boolean isInstantiated() {
        return owner != null;
    }

    public Class getOwnerClass() {
        return ownerClass;
    }

    /**
     * Returns the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     */
    public Component getOwner() {
        return getOwner(null, null);
    }

    /**
     * Gets the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param ps the property sheet that caused the getOwner call for this
     * property sheet.
     */
    public synchronized Component getOwner(PropertySheet ps) {
        return getOwner(ps, null, true);
    }

    /**
     * Gets the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param ps the property sheet that caused the getOwner call for this
     * property sheet.
     * @param reuseComponent if <code>true</code>, a previously configured
     * component will be returned, if there is one. If <code>false</code> a
     * newly instantiated and configured component will be returned.
     */
    public synchronized Component getOwner(PropertySheet ps, boolean reuseComponent) {
        return getOwner(ps, null, reuseComponent);
    }

    /**
     * Returns the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param ps the property sheet that caused the getOwner call for this
     * property sheet.
     */
    public synchronized Component getOwner(PropertySheet ps, ComponentListener cl) {
        return getOwner(ps, cl, true);
    }
    
    private InputStream getSerializedStream(String sf) {
        //
        // First, see if it's a resource on our classpath.
        InputStream ret = this.getClass().getResourceAsStream(sf);
        if(ret == null) {
            try {
                //
                // Nope. See if it's a valid URL and open that.
                URL sfu = new URL(sf);
                ret = sfu.openStream();
            } catch (MalformedURLException ex) {
                try {
                    //
                    // Not a valid URL, so try it as a file name.
                    ret = new FileInputStream(sf);
                } catch (FileNotFoundException ex1) {
                    //
                    // Couldn't open the file, we're done.
                    return null;
                }
            } catch (IOException ex) {
                //
                // No joy.
                logger.warning("Cannot open serialized form " + sf);
                return null;
            }
        }
        return ret;
    }

    public synchronized Component getOwner(PropertySheet ps, ComponentListener cl, boolean reuseComponent) {
        try {

            if (!isInstantiated() || !reuseComponent) {

                ComponentRegistry registry = cm.getComponentRegistry();
                //
                // See if we should do a lookup in a service registry.
                if (registry != null
                        && !isExportable()
                        && ((size() == 0 && implementsRemote) || isImportable())) {
                    if (logger != null && logger.isLoggable(Level.FINER)) {
                        logger.finer(String.format("Looking up instance %s in registry",
                                getInstanceName()));
                    }
                    owner = cm.getComponentRegistry().lookup(this, cl);
                    if (owner != null) {
                        return owner;
                    } else if (size() == 0 && isImportable()) {
                        //
                        // We needed to look something up and no success,
                        // so return null.
                        return null;
                    }
                }
                
                //
                // Should we load a serialized form?
                if(serializedForm != null) {
                    InputStream serStream = getSerializedStream(serializedForm);
                    if(serStream != null) {
                        ObjectInputStream ois;
                        try {
                            //
                            // Read the object and cast it into this class for return;
                            ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024*1024));
                            Object deser = ois.readObject();
                            ois.close();
                            return ownerClass.cast(deser);
                        } catch (IOException ex) {
                            throw new PropertyException(ex, ps.getInstanceName(), null, "Error reading serialized form");
                        } catch (ClassNotFoundException ex) {
                            throw new PropertyException(ex, ps.getInstanceName(), null, "Serialized class not found");
                        }
                    }
                }
                
                if (logger != null && logger.isLoggable(Level.FINER)) {
                    logger.finer(String.format("Creating %s", getInstanceName()));
                }
                if (cm.showCreations) {
                    logger.info(String.format("Creating %s type %s", instanceName,
                            ownerClass.getName()));
                }
                owner = ownerClass.newInstance();
                setConfiguredFields(owner, this);
                if (owner instanceof Configurable) {
                    ((Configurable) owner).newProperties(this);
                }
                if (owner instanceof ConfigurableMXBean) {
                    MBeanServer mbs = cm.getMBeanServer();
                    String on = String.format("%s:type=%s,name=%s",
                            ownerClass.getPackage().getName(),
                            ownerClass.getSimpleName(),
                            instanceName);
                    try {
                        ObjectName oname = new ObjectName(on);
                        if (mbs != null) {
                            mbs.registerMBean(owner, oname);
                        }
                    } catch (Exception e) {
                        throw new PropertyException(e, ps.getInstanceName(),
                                null,
                                null);
                    }
                }
                if (registry != null && isExportable()) {
                    registry.register(owner, this);
                }
            }
        } catch (IllegalAccessException e) {
            throw new InternalConfigurationException(e, getInstanceName(), null, "Can't access class "
                    + ownerClass);
        } catch (InstantiationException e) {
            throw new InternalConfigurationException(e, getInstanceName(), null, "Can't instantiate class "
                    + ownerClass);
        }

        return owner;
    }

    /**
     * Sets the variables in the class that are annotated using the @Config
     * annotation (if there are any), using the values given in the property
     * sheet.
     *
     * @param o the object we're setting values for
     * @param ownerClass the class of the object we're setting values for
     * @param ps the property sheet with the values that we want to set.
     */
    private void setConfiguredFields(Object o, PropertySheet ps) throws PropertyException, IllegalAccessException {
        
        for (Field f : o.getClass().getDeclaredFields()) {
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            for (Annotation a : f.getAnnotations()) {
                if (a instanceof Config) {
                    //
                    // We have a variable annotated with the Config annotation, 
                    // let's get a value out of the property sheet and figure 
                    // out how to turn it into the right type.
                    
                    //
                    // We'll handle things that have lists with items separately.
                    FieldType ft = FieldType.getFieldType(f);
                    if(FieldType.listTypes.contains(ft)) {
                        List<String> vals = (List<String>) ps.propValues.get(f.getName());
                        List<String> replaced = new ArrayList<String>();
                        for(String val : vals) {
                            replaced.add(ps.getConfigurationManager().getGlobalProperties().replaceGlobalProperties(getInstanceName(), f.getName(), val));
                        }
                        switch(ft) {
                            case STRING_ARRAY:
                                f.set(o, replaced.toArray(new String[0]));
                                break;
                            case COMPONENT_ARRAY:
                                Component[] cs = new Component[replaced.size()];
                                for(int i = 0; i < cs.length; i++) {
                                    cs[i] = ps.getConfigurationManager().lookup(replaced.get(i));
                                }
                                f.set(o, cs);
                                break;
                            case CONFIGURABLE_ARRAY:
                                Configurable[] cos = new Configurable[replaced.size()];
                                for(int i = 0; i < cos.length; i++) {
                                    cos[i] = (Configurable) ps.getConfigurationManager().lookup(replaced.get(i));
                                }
                                f.set(o, cos);
                                break;
                        }
                        continue;
                    }
                    
                    //
                    // We know it's a single string now. 
                    // We'll use flattenProp so that we take care of any variables
                    // in the value.
                    String val = ps.flattenProp(f.getName());
                    
                    //
                    // Handle empty values.
                    if(val == null) {
                        if(((Config) a).mandatory()) {
                            throw new PropertyException(ps.getInstanceName(), f.getName(), f.getName() + " is mandatory in configuration");
                        } else {
                            continue;
                        }
                    }
                    switch (ft) {
                        case STRING:
                            f.set(o, val);
                            break;
                        case BOOLEAN:
                            f.setBoolean(o, Boolean.parseBoolean(val));
                            break;
                        case INTEGER:
                            try {
                                f.setInt(o, Integer.parseInt(val));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not an integer", val));
                            }
                            break;
                        case ATOMIC_INTEGER:
                            try {
                                f.set(o, new AtomicInteger(Integer.parseInt(val)));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not an integer", val));
                            }
                            break;
                        case INTEGER_ARRAY:
                            try {
                                String[] vals = arraySplit.split(val);
                                int[] ia = new int[vals.length];
                                int i = 0;
                                for (String v : vals) {
                                    ia[i++] = Integer.parseInt(v);
                                }
                                f.set(o, ia);
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not an integer", val));
                            }
                            break;
                        case LONG:
                            try {
                                f.setLong(o, Long.parseLong(val));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not a long", val));
                            }
                            break;
                        case ATOMIC_LONG:
                            try {
                                f.set(o, new AtomicLong(Long.parseLong(val)));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not a long", val));
                            }
                            break;
                        case LONG_ARRAY:
                            try {
                                String[] vals = arraySplit.split(val);
                                long[] la = new long[vals.length];
                                int i = 0;
                                for (String v : vals) {
                                    la[i++] = Long.parseLong(v);
                                }
                                f.set(o, la);
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not an array of long", val));
                            }
                            break;
                        case FLOAT:
                            try {
                                f.setFloat(o, Float.parseFloat(val));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not an float", val));
                            }
                            break;
                        case FLOAT_ARRAY:
                            try {
                                String[] vals = arraySplit.split(val);
                                float[] fa = new float[vals.length];
                                int i = 0;
                                for (String v : vals) {
                                    fa[i++] = Float.parseFloat(v);
                                }
                                f.set(o, fa);
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s does not specify an array of float", val));
                            }
                            break;
                        case DOUBLE:
                            try {
                                f.setDouble(o, Double.parseDouble(val));
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not a double", val));
                            }
                            break;
                        case DOUBLE_ARRAY:
                            try {
                                String[] vals = arraySplit.split(val);
                                double[] da = new double[vals.length];
                                int i = 0;
                                for (String v : vals) {
                                    da[i++] = Long.parseLong(v);
                                }
                                f.set(o, da);
                            } catch (NumberFormatException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s does not specify an array of double", val));
                            }
                            break;
                        case ENUM:
                            try {
                                f.set(o, Enum.valueOf((Class<Enum>) f.getType(), val));
                            } catch (IllegalArgumentException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s is not a value of %s", val, f.getClass()));
                            }
                            break;
                        case ENUM_SET:
                            try {
                                EnumSet s = EnumSet.noneOf((Class<Enum>) f.getType());
                                String[] vals = arraySplit.split(val.toUpperCase());
                                for(String v : vals) {
                                    s.add(Enum.valueOf((Class<Enum>) f.getType(), v));
                                }
                                f.set(o, s);
                            } catch (IllegalArgumentException ex) {
                                throw new PropertyException(ex, ps.instanceName, f.getName(), String.format("%s has values not in %s", val, f.getClass()));
                            }
                            break;
                        case FILE:
                            f.set(o, new File(val));
                            break;
                        case PATH:
                            f.set(0, Paths.get(val));
                            break;
                        case COMPONENT:
                            f.set(o, ps.getConfigurationManager().lookup(f.getName()));
                            break;
                    }
                }
            }
            f.setAccessible(accessible);
        }

    }

    protected void clearOwner() {
        owner = null;
    }

    /**
     * Returns the class of the owner configurable of this property sheet.
     */
    public Class<? extends Component> getConfigurableClass() {
        return ownerClass;
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     */
    public void setString(String name, String value) throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigString;

        applyConfigurationChange(name, value, value);
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     * @param value the value for the property
     */
    public void setInt(String name, int value) throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigInteger;

        applyConfigurationChange(name, value, value);
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     * @param value the value for the property
     */
    public void setDouble(String name, double value) throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigDouble;

        applyConfigurationChange(name, value, value);
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     * @param value the value for the property
     */
    public void setBoolean(String name, Boolean value) throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigBoolean;

        applyConfigurationChange(name, value, value);
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     * @param cmName the name of the configurable within the configuration
     * manager (required for serialization only)
     * @param value the value for the property
     */
    public void setComponent(String name, String cmName, Component value)
            throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigComponent;

        applyConfigurationChange(name, cmName, value);
    }

    /**
     * Sets the given property to the given name
     *
     * @param name the simple property name
     * @param valueNames the list of names of the configurables within the
     * configuration manager (required for serialization only)
     * @param value the value for the property
     */
    public void setComponentList(String name, List<String> valueNames,
            List<Configurable> value) throws PropertyException {
        // ensure that there is such a property
        assert registeredProperties.keySet().contains(name) : "'" + name
                + "' is not a registered compontent";

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        assert annotation instanceof ConfigComponentList;

        //        assert valueNames.size() == value.size();
        rawProps.put(name, valueNames);
        propValues.put(name, value);

        applyConfigurationChange(name, valueNames, value);
    }

    public void setStringList(String name, List<String> values) {
        if (!registeredProperties.containsKey(name)) {
            throw new PropertyException(instanceName, name, name + " is not a"
                    + "registered property");
        }

        Proxy annotation = registeredProperties.get(name).getAnnotation();
        if (!(annotation instanceof ConfigStringList)) {
            throw new PropertyException(instanceName, name, name + " is not a"
                    + "string list property");
        }

        rawProps.put(name, values);
        propValues.put(name, values);

        applyConfigurationChange(name, values, values);

    }

    private void applyConfigurationChange(String propName, Object cmName,
            Object value) throws PropertyException {
        rawProps.put(propName, cmName);
        propValues.put(propName, value);

        if (getInstanceName() != null) {
            cm.fireConfChanged(getInstanceName(), propName);
        }

        if (owner != null && owner instanceof Configurable) {
            ((Configurable) owner).newProperties(this);
        }

    }

    /**
     * Sets the raw property to the given name
     *
     * @param key the simple property name
     * @param val the value for the property
     */
    public void setRaw(String key, Object val) {
        rawProps.put(key, val);
    }

    /**
     * Gets the raw value associated with this name
     *
     * @param name the name
     * @return the value as an object (it could be a String or a String[]
     * depending upon the property type)
     */
    public Object getRaw(String name) {
        return rawProps.get(name);
    }

    /**
     * Gets the raw value associated with this name, no global symbol
     * replacement is performed.
     *
     * @param name the name
     * @return the value as an object (it could be a String or a String[]
     * depending upon the property type)
     */
    public Object getRawNoReplacement(String name) {
        return rawProps.get(name);
    }

    /**
     * Returns the type of the given property.
     */
    public PropertyType getType(String propName) {
        Proxy annotation = registeredProperties.get(propName).getAnnotation();
        if (annotation instanceof ConfigComponent) {
            return PropertyType.COMP;
        } else if (annotation instanceof ConfigComponentList) {
            return PropertyType.COMPLIST;
        } else if (annotation instanceof ConfigInteger) {
            return PropertyType.INT;
        } else if (annotation instanceof ConfigEnum) {
            return PropertyType.ENUM;
        } else if (annotation instanceof ConfigEnumSet) {
            return PropertyType.ENUM;
        } else if (annotation instanceof ConfigDouble) {
            return PropertyType.DOUBLE;
        } else if (annotation instanceof ConfigBoolean) {
            return PropertyType.BOOL;
        } else if (annotation instanceof ConfigString) {
            return PropertyType.STRING;
        } else if (annotation instanceof ConfigStringList) {
            return PropertyType.STRINGLIST;
        } else {
            throw new RuntimeException("Unknown property type");
        }

    }

    /**
     * Gets the owning property manager
     *
     * @return the property manager
     */
    public ConfigurationManager getConfigurationManager() {
        return cm;
    }

    /**
     * Gets the size of the property sheet, that is, the number of properties
     * that it contains.
     */
    public int size() {
        return propValues.size();
    }

    /**
     * Returns a logger to use for this configurable component. The logger can
     * be configured with the property: 'logLevel' - The default logLevel value
     * is defined (within the xml configuration file by the global property
     * 'defaultLogLevel' (which defaults to WARNING).
     * <p/>
     * implementation note: the logger became configured within the constructor
     * of the parenting configuration manager.
     *
     * @return the logger for this component
     */
    public Logger getLogger() {
        if (logger != null) {
            return logger;
        }

        //
        // Get a logger for this particular named instance, if we can.
        if (instanceName != null) {
            logger = Logger.getLogger(ownerClass.getName() + "." + instanceName);
        } else {
            logger = Logger.getLogger(ownerClass.getName());
        }

        //
        // If there's a log level defined for the component, then we can use that,
        // otherwise we'll use the global level.  In either case, we need to make
        // sure that the provided level is a legal level.
        logLevel = getLogLevel();
        if (logLevel != null) {
            logger.setLevel(logLevel);
        }
        return logger;
    }

    public Level getLogLevel() {
        if (logLevel == null) {
            String lls = (String) rawProps.get(PROP_LOG_LEVEL);
            if (lls == null) {
                lls = cm.getGlobalProperty(
                        ConfigurationManagerUtils.GLOBAL_COMMON_LOGLEVEL);
            }

            if (lls != null) {
                try {
                    logLevel = Level.parse(lls);
                } catch (IllegalArgumentException iae) {
                    throw new PropertyException(instanceName, PROP_LOG_LEVEL,
                            "Globa log level, "
                            + lls + " is not a valid log level");
                }
            } else {
                //
                // When all else fails, choose INFO.
                logLevel = Level.INFO;
            }
        }
        return logLevel;
    }

    /**
     * Returns the names of registered properties of this PropertySheet object.
     */
    public Collection<String> getRegisteredProperties() {
        return Collections.unmodifiableCollection(registeredProperties.keySet());
    }

    public void setCM(ConfigurationManager cm) {
        this.cm = cm;
    }

    /**
     * Returns true if two property sheet define the same object in terms of
     * configuration. The owner (and the parent configuration manager) are not
     * expected to be the same.
     */
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PropertySheet)) {
            return false;
        }

        PropertySheet ps = (PropertySheet) obj;
        if (!rawProps.keySet().equals(ps.rawProps.keySet())) {
            return false;
        }

// maybe we could test a little bit more here. suggestions?
        return true;
    }

    protected Object clone() throws CloneNotSupportedException {
        PropertySheet ps = (PropertySheet) super.clone();

        ps.registeredProperties
                = new HashMap<String, ConfigPropWrapper>(this.registeredProperties);
        ps.propValues = new HashMap<String, Object>(this.propValues);

        ps.rawProps = new HashMap<String, Object>(this.rawProps);

        // make deep copy of raw-lists
        for (String regProp : ps.getRegisteredProperties()) {
            if (getType(regProp).equals(PropertyType.COMPLIST)) {
                ps.rawProps.put(regProp,
                        new ArrayList<String>((Collection<? extends String>) rawProps.get(regProp)));
                ps.propValues.put(regProp, null);
            }

        }

        ps.cm = cm;
        ps.owner = null;
        ps.instanceName = this.instanceName;

        return ps;
    }
    
    /**
     * Gets all of the fields associated with a class by walking up the
     * class tree.  Handles super classes, as well as interfaces.
     * @param configurable the class who's fields we wish to walk.
     * @return all of the fields, so they can be checked for annoatations.
     */
    private Collection<Field> getAllFields(Class configurable) {
        Set<Field> ret = new HashSet<>();
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(configurable);
        while(!cq.isEmpty()) {
            Class curr = cq.remove();
            ret.addAll(Arrays.asList(curr.getDeclaredFields()));
            ret.addAll(Arrays.asList(curr.getFields()));
            Class sc = curr.getSuperclass();
            if(sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));
        }
        return ret;
    }

    /**
     * use annotation based class parsing to detect the configurable properties
     * of a <code>Configurable</code>-class
     *
     * @param propertySheet of type PropertySheet
     * @param configurable of type Class<? extends Configurable>
     */
    public void processAnnotations(PropertySheet propertySheet,
            Class<? extends Configurable> configurable) throws PropertyException {
        
        //
        // This is kind of a hack to handle Scala classes that want to be 
        // configurable. The convention in Scala is to annotate a method that
        // returns a string that is the name of the variable in the configuration file
        // like so:
        // @ConfigInteger(defaultValue=128)
        // def PROP_MY_INTEGER = "myInteger"
        //
        // Note that we're going to need to invoke the method, so we need an
        // instance, so we'll gin one up if necessary.
        Object configurableInstance = null;
        for (Method method : configurable.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                for (Annotation superAnnotation : annotation.annotationType().getAnnotations()) {
                    try {
                        if (!(superAnnotation instanceof ConfigProperty)) {
                            continue;
                        }
                        if (configurableInstance == null) {
                            configurableInstance = configurable.newInstance();
                        }
                        //
                        // OK, call the method to get the name of the string.
                        String propertyName = (String) method.invoke(configurableInstance, (Object[]) null);
                        propertySheet.registerProperty(propertyName,
                                new ConfigPropWrapper((Proxy) annotation));
                    } catch (IllegalAccessException | IllegalArgumentException |
                            InvocationTargetException | InstantiationException ex) {
                        throw new PropertyException(ex, propertySheet.instanceName, method.getName(),
                                "Error invoking configurable method: "
                                + method.getName());
                    }
                }
            }
        }

        //
        // The java version.
        Collection<Field> classFields = getAllFields(configurable);
        
        for (Field field : classFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Config) {
                    //
                    // We have a variable annotated with the Config annotation.

                    propertySheet.registerProperty(field.getName(), new ConfigPropWrapper((Proxy) annotation));

                } else {
                    Annotation[] superAnnotations = annotation.annotationType().
                            getAnnotations();

                    for (Annotation superAnnotation : superAnnotations) {
                        if (superAnnotation instanceof ConfigProperty) {
                            int fieldModifiers = field.getModifiers();

                            //
                            // Make sure the annotated property name is a public static
                            // String.  Throw an exception if this is not the case.
                            if (!Modifier.isStatic(fieldModifiers)) {
                                throw new PropertyException(propertySheet.getInstanceName(),
                                        field.getName(),
                                        "property fields are assumed to be static");
                            }
                            if (!Modifier.isPublic(fieldModifiers)) {
                                throw new PropertyException(propertySheet.getInstanceName(),
                                        field.getName(),
                                        "property fields are assumed to be public");
                            }
                            if (!field.getType().equals(String.class)) {
                                throw new PropertyException(propertySheet.getInstanceName(),
                                        field.getName(),
                                        "properties fields are assumed to be instances of java.lang.String");
                            }

                            try {
                                String propertyName = (String) field.get(null);
                                propertySheet.registerProperty(propertyName,
                                        new ConfigPropWrapper((Proxy) annotation));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    protected void save(PrintWriter writer) {
        writer.printf("\t<component name=\"%s\" type=\"%s\" export=\"%s\" "
                + "import=\"%s\">\n",
                instanceName,
                getConfigurableClass().getName(),
                isExportable(),
                isImportable());

        for (String propName : getRegisteredProperties()) {
            if (getRawNoReplacement(propName) == null) {
                continue;
            }  // if the property was net defined within the xml-file

            switch (getType(propName)) {

                case COMPLIST:
                case STRINGLIST:
                    writer.printf("\t\t<propertylist name=\"%s\">\n", propName);
                    for (Object o : (List) getRawNoReplacement(propName)) {
                        if (o instanceof Class) {
                            writer.printf("\t\t\t<type>%s</type>\n", ((Class) o).getName());
                        } else {
                            writer.printf("\n\t\t\t<item>%s</item>\n", o);
                        }
                    }
                    writer.println("\n\t\t</propertylist>");
                    break;
                case ENUMSET:
                    writer.printf("\t\t<propertylist name=\"%s\">\n", propName);
                    for (Object o : (EnumSet) getRawNoReplacement(propName)) {
                        if (o instanceof Class) {
                            writer.printf("\t\t\t<type>%s</type>\n", ((Class) o).
                                    getName());
                        } else {
                            writer.printf("\n\t\t\t<item>%s</item>\n", o);
                        }
                    }
                    writer.println("\n\t\t</propertylist>");
                    break;
                default:
                    writer.printf("\t\t<property name=\"%s\" value=\"%s\"/>\n",
                            propName,
                            getRawNoReplacement(propName));
            }
        }

        writer.println("\t</component>\n");

    }

}
