package com.sun.labs.util.props;

import net.jini.core.lease.Lease;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A property sheet which defines a collection of properties for a single
 * component in the system.
 */
public class PropertySheet implements Cloneable {
    private static final Logger logger = Logger.getLogger(PropertySheet.class.getName());

    public enum PropertyType { CONFIG, COMPNAME, CONMAN; }

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

    private Configurable owner;

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

    private final Class<? extends Configurable> ownerClass;

    private String instanceName;


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
        // then throw a property exception.
        for (String propName : rpd.getProperties().keySet()) {
            if (!propValues.containsKey(propName)) {
                throw new PropertyException(instanceName, propName,
                        "Unknown property in configuration file.");
            }
        }

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
        Set<String> ps = new HashSet<String>(rawProps.keySet());
        return ps;
    }

    /**
     * Registers a new property which type and default value are defined by the
     * given sphinx property.
     *
     * @param propName The name of the property to be registered.
     * @param property The property annotation masked by a proxy.
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
                    + " is not an annotated property of '" + getConfigurableClass().
                    getName() + "' !");
        }

        return s4PropWrapper;
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
    public Configurable getOwner() {
        return getOwner(null, null);
    }

    /**
     * Gets the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param ps the property sheet that caused the getOwner call for this
     * property sheet.
     */
    public synchronized Configurable getOwner(PropertySheet ps) {
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
    public synchronized Configurable getOwner(PropertySheet ps, boolean reuseComponent) {
        return getOwner(ps, null, reuseComponent);
    }

    /**
     * Returns the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param ps the property sheet that caused the getOwner call for this
     * property sheet.
     */
    public synchronized Configurable getOwner(PropertySheet ps, ComponentListener cl) {
        return getOwner(ps, cl, true);
    }

    public synchronized Configurable getOwner(PropertySheet ps, ComponentListener cl, boolean reuseComponent) {
        try {
            logger.setLevel(Level.ALL);

            if (!isInstantiated() || !reuseComponent) {

                ComponentRegistry registry = cm.getComponentRegistry();
                //
                // See if we should do a lookup in a service registry.
                if (registry != null
                        && !isExportable()
                        && ((size() == 0 && implementsRemote) || isImportable())) {
                        logger.finer(String.format("Looking up instance %s in registry",
                                getInstanceName()));
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
                if (serializedForm != null) {
                    String actualLocation = cm.getGlobalProperties().replaceGlobalProperties(ps.getInstanceName(), null, serializedForm);
                    InputStream serStream = cm.getInputStreamForLocation(actualLocation);
                    if (serStream != null) {
                        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024 * 1024))) {
                            Object deser = ois.readObject();
                            owner = ownerClass.cast(deser);
                            return owner;
                        } catch (IOException ex) {
                            throw new PropertyException(ex, 
                                    ps.getInstanceName(), null, 
                                    "Error reading serialized form from " + actualLocation);
                        } catch (ClassNotFoundException ex) {
                            throw new PropertyException(ex, 
                                    ps.getInstanceName(), null, 
                                    "Serialized class not found at " + actualLocation);
                        }
                    }
                }

                logger.finer(String.format("Creating %s", getInstanceName()));
                if (cm.showCreations) {
                    logger.info("CM using:");
                    for (URL u : cm.getConfigURLs()) {
                        logger.info(u.toString());
                    }
                    logger.info(String.format("Creating %s type %s", instanceName,
                            ownerClass.getName()));
                }
                try {
                    Constructor<? extends Configurable> constructor = ownerClass.getDeclaredConstructor();
                    boolean isAccessible = constructor.isAccessible();
                    constructor.setAccessible(true);
                    owner = constructor.newInstance();
                    constructor.setAccessible(isAccessible);
                } catch (NoSuchMethodException ex) {
                    throw new PropertyException(ex, ps.getInstanceName(), null,
                            "No-args constructor not found for class " + ownerClass);
                } catch (InvocationTargetException ex) {
                    throw new InternalConfigurationException(ex, ps.getInstanceName(), null,
                            "Can't instantiate class " + ownerClass);
                }
                setConfiguredFields(owner, this);
                try {
                    owner.postConfig();
                } catch (IOException e) {
                    throw new PropertyException(e, instanceName, null, "IOException thrown by postConfig");
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
     * @param ps the property sheet with the values that we want to set.
     */
    private static void setConfiguredFields(Object o, PropertySheet ps) throws PropertyException, IllegalAccessException {

        Class<?> curClass = o.getClass();
        Set<Field> fields = getAllFields(curClass);
        for (Field f : fields) {
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            for (Annotation a : f.getAnnotations()) {
                if (a instanceof Config) {
                    //
                    // We have a variable annotated with the Config annotation,
                    // let's get a value out of the property sheet and figure
                    // out how to turn it into the right type.
                    FieldType ft = FieldType.getFieldType(f);
                    if (ft == null) {
                        throw new PropertyException(ps.getInstanceName(), f.getName(), f.getName() + " has an unknown field type");
                    }
                    logger.log(Level.FINEST,"Found field of type " + ft.name());
                    //
                    // Handle empty values.
                    if (ps.propValues.get(f.getName()) == null) {
                        if (((Config) a).mandatory()) {
                            throw new PropertyException(ps.getInstanceName(), f.getName(), f.getName() + " is mandatory in configuration");
                        } else {
                            continue;
                        }
                    }

                    //
                    // We'll handle things that have list or arrays with items separately.
                    if (FieldType.listTypes.contains(ft)) {
                        List vals = (List) ps.propValues.get(f.getName());
                        f.set(o, parseListField(f.getName(), f.getType(), (Config) a, ft, ps, vals));
                    } else if (FieldType.simpleTypes.contains(ft)) {
                        //
                        // We'll use flattenProp so that we take care of any variables
                        // in the single value.
                        String val = ps.flattenProp(f.getName());
                        f.set(o, parseSimpleField(f.getName(), f.getType(), ft, ps, val));
                    } else {
                        //
                        // Last option is a map, as it's not a single value or a list.
                        Map<String, String> mapVals = (Map<String, String>) ps.propValues.get(f.getName());
                        f.set(o, parseMapField(f.getName(), (Config) a, ps, mapVals));
                    }
                } else if (a instanceof ConfigurableName) {
                    if (String.class.isAssignableFrom(f.getType())) {
                        f.set(o, ps.getInstanceName());
                    } else {
                        throw new PropertyException(ps.getInstanceName(), f.getName(), "Assigning ConfigurableName to non-String type " + f.getType().getName());
                    }
                } else if (a instanceof ConfigManager) {
                    if (ConfigurationManager.class.isAssignableFrom(f.getType())) {
                        f.set(o, ps.getConfigurationManager());
                    } else {
                        throw new PropertyException(ps.getInstanceName(), f.getName(), "Assigning ConfigManager to non-ConfigurationManager type " + f.getType().getName());
                    }
                }
            }
            f.setAccessible(accessible);
        }
    }

    private static Map parseMapField(String fieldName, Config fieldAnnotation, PropertySheet ps, Map<String,String> input) {
        Class<?> genericType = fieldAnnotation.genericType();
        FieldType genericft = FieldType.getFieldType(genericType);
        Map map = new HashMap<>();
        for (Map.Entry<String, String> e : input.entrySet()) {
            String newVal = ps.getConfigurationManager().getGlobalProperties().replaceGlobalProperties(ps.instanceName, fieldName, e.getValue());
            map.put(e.getKey(), parseSimpleField(fieldName, genericType, genericft, ps, newVal));
        }
        return map;
    }

    private static Object parseListField(String fieldName, Class<?> fieldClass, Config fieldAnnotation, FieldType ft, PropertySheet ps, List input) {
        //
        // This dance happens as some of the list types support class values,
        // and this is the first place where FieldType meets the value loaded
        // from the xml file, so we have to check it.
        List<String> replaced = new ArrayList<>();
        List<Class<?>> classVals = new ArrayList<>();
        List<Class<?>> removeList = new ArrayList<>();
        for (Object val : input) {
            if (val instanceof String) {
                replaced.add(ps.getConfigurationManager().getGlobalProperties().replaceGlobalProperties(ps.instanceName, fieldName, (String) val));
            } else if (val instanceof Class) {
                classVals.add((Class<?>) val);
            } else {
                throw new PropertyException(ps.instanceName,fieldName,"Unknown type loaded from property, found " + val.getClass().getName());
            }
        }

        //
        // Now go through the valid list types and assign the output field.
        Object output = null;
        switch (ft) {
            case STRING_ARRAY:
                output = replaced.toArray(new String[0]);
                break;
            case CONFIGURABLE_ARRAY:
                List<Configurable> configurableList = new ArrayList<>();
                Class<?> configArrayType = fieldClass.getComponentType();
                for (String name : replaced) {
                    Configurable c = ps.getConfigurationManager().lookup(name);
                    if (c == null) {
                        throw new PropertyException(ps.getInstanceName(), fieldName, fieldName + " looked up an unknown configurable called " + name);
                    }
                    configurableList.add(c);
                }
                for (Class c : classVals) {
                    if (configArrayType.isAssignableFrom(c)) {
                        configurableList.addAll(ps.getConfigurationManager().lookupAll(c, null));
                        removeList.add(c);
                    } else {
                        throw new PropertyException(ps.instanceName, fieldName, "Unassignable class " + c.getName() + " to configArrayType " + configArrayType.getName());
                    }
                }
                Configurable[] cos = (Configurable[]) Array.newInstance(configArrayType, configurableList.size());
                for (int i = 0; i < configurableList.size(); i++) {
                    cos[i] = configurableList.get(i);
                }
                output = cos;
                break;
            case BYTE_ARRAY:
                try {
                    byte[] ia = new byte[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        ia[i++] = Byte.parseByte(v);
                    }
                    output = ia;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a byte", replaced.toString()));
                }
                break;
            case SHORT_ARRAY:
                try {
                    short[] ia = new short[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        ia[i++] = Short.parseShort(v);
                    }
                    output = ia;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a short", replaced.toString()));
                }
                break;
            case INTEGER_ARRAY:
                try {
                    int[] ia = new int[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        ia[i++] = Integer.parseInt(v);
                    }
                    output = ia;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not an integer", replaced.toString()));
                }
                break;
            case LONG_ARRAY:
                try {
                    long[] la = new long[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        la[i++] = Long.parseLong(v);
                    }
                    output = la;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not an array of long", replaced.toString()));
                }
                break;
            case FLOAT_ARRAY:
                try {
                    float[] fa = new float[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        fa[i++] = Float.parseFloat(v);
                    }
                    output = fa;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s does not specify an array of float", replaced.toString()));
                }
                break;
            case DOUBLE_ARRAY:
                try {
                    double[] da = new double[replaced.size()];
                    int i = 0;
                    for (String v : replaced) {
                        da[i++] = Double.parseDouble(v);
                    }
                    output = da;
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s does not specify an array of double", replaced.toString()));
                }
                break;
            case ENUM_SET:
                try {
                    Class<Enum> enumType = (Class<Enum>) fieldAnnotation.genericType();
                    EnumSet s = EnumSet.noneOf(enumType);
                    for (String v : replaced) {
                        s.add(Enum.valueOf(enumType, v.toUpperCase()));
                    }
                    output = s;
                } catch (ClassCastException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("The supplied type %s is not an Enum type", fieldAnnotation.genericType().toString()));
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s has values not in %s", replaced.toString(), fieldClass));
                }
                break;
            case LIST:
                try {
                    Class<?> genericType = fieldAnnotation.genericType();
                    FieldType genericft = FieldType.getFieldType(genericType);
                    List list = new ArrayList(replaced.size());
                    for (String v : replaced) {
                        list.add(parseSimpleField(fieldName, genericType, genericft, ps, v));
                    }
                    for (Class c : classVals) {
                        if (genericType.isAssignableFrom(c)) {
                            list.addAll(ps.getConfigurationManager().lookupAll(c, null));
                            removeList.add(c);
                        } else {
                            throw new PropertyException(ps.instanceName, fieldName, "Unassignable class " + c.getName() + " to genericType " + genericType.getName());
                        }
                    }
                    output = list;
                } catch (ClassCastException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("The supplied genericType %s does not match the type of the object", fieldAnnotation.genericType().toString()));
                }
                break;
            case SET:
                try {
                    Class<?> genericType = fieldAnnotation.genericType();
                    FieldType genericft = FieldType.getFieldType(genericType);
                    Set set = new HashSet(replaced.size());
                    for (String v : replaced) {
                        set.add(parseSimpleField(fieldName, genericType, genericft, ps, v));
                    }
                    for (Class c : classVals) {
                        if (genericType.isAssignableFrom(c)) {
                            set.addAll(ps.getConfigurationManager().lookupAll(c, null));
                            removeList.add(c);
                        } else {
                            throw new PropertyException(ps.instanceName, fieldName, "Unassignable class " + c.getName() + " to genericType " + genericType.getName());
                        }
                    }
                    output = set;
                } catch (ClassCastException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("The supplied genericType %s does not match the type of the object", fieldAnnotation.genericType().toString()));
                }
                break;
        }
        classVals.removeAll(removeList);
        if (classVals.size() > 0) {
            throw new PropertyException(ps.instanceName,fieldName,"Found class values in a primitive array");
        }
        return output;
    }

    private static Object parseSimpleField(String fieldName, Class<?> fieldClass, FieldType ft, PropertySheet ps, String val) {
        switch (ft) {
            case STRING:
                return val;
            case BOOLEAN:
                return Boolean.parseBoolean(val);
            case BYTE:
                try {
                    return Byte.parseByte(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a byte", val));
                }
            case SHORT:
                try {
                    return Short.parseShort(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a short", val));
                }
            case INTEGER:
                try {
                    return Integer.parseInt(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not an integer", val));
                }
            case ATOMIC_INTEGER:
                try {
                    return new AtomicInteger(Integer.parseInt(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not an integer", val));
                }
            case LONG:
                try {
                    return Long.parseLong(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a long", val));
                }
            case ATOMIC_LONG:
                try {
                    return new AtomicLong(Long.parseLong(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a long", val));
                }
            case FLOAT:
                try {
                    return Float.parseFloat(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not an float", val));
                }
            case DOUBLE:
                try {
                    return Double.parseDouble(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a double", val));
                }
            case FILE:
                return new File(val);
            case PATH:
                return Paths.get(val);
            case RANDOM:
                try {
                    return new Random(Integer.parseInt(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("The seed %s is not an integer", val));
                }
            case ENUM:
                try {
                    return Enum.valueOf((Class<Enum>) fieldClass, val);
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException(ex, ps.instanceName, fieldName, String.format("%s is not a value of %s", val, fieldClass));
                }
            case CONFIGURABLE:
                Configurable comp = ps.getConfigurationManager().lookup(val);
                if (comp == null) {
                    throw new PropertyException(ps.getInstanceName(), fieldName, fieldName + " looked up an unknown component called " + val);
                }
                return comp;
            default:
                throw new PropertyException(ps.getInstanceName(), fieldName, fieldName + " was not a simple configurable field");
        }
    }

    /**
     * Checks this PropertySheet for the supplied fieldName, and returns
     * the parsed value. Returns null if the property is not set in the sheet,
     * and throws PropertyException if it's mandatory.
     *
     * Note: does not read default values from class files, it only returns values
     * from the configuration.
     * @param fieldName The field name to lookup.
     * @return The field value parsed out of the configuration.
     */
    public Object get(String fieldName) throws PropertyException {
        Set<Field> fields = getAllFields(ownerClass);
        for (Field f : fields) {
            if (f.getName().equals(fieldName) && (f.getAnnotation(Config.class) != null)) {
                Config fieldAnnotation = f.getAnnotation(Config.class);
                //
                // Field exists in object, now check the property sheet.
                // Handle empty values.

                if (propValues.get(f.getName()) == null) {
                    if (fieldAnnotation.mandatory()) {
                        throw new PropertyException(getInstanceName(), f.getName(), f.getName() + " is mandatory in configuration");
                    } else {
                        return null;
                    }
                } else {
                    FieldType ft = FieldType.getFieldType(f);
                    if (FieldType.listTypes.contains(ft)) {
                        return parseListField(f.getName(),f.getType(),fieldAnnotation,ft,this,(List)propValues.get(f.getName()));
                    } else if (FieldType.simpleTypes.contains(ft)) {
                        return parseSimpleField(f.getName(),f.getType(),ft,this, flattenProp(f.getName()));
                    } else {
                        return parseMapField(f.getName(),fieldAnnotation,this, (Map<String,String>)propValues.get(f.getName()));
                    }
                }
            }

        }
        return null;
    }

    /**
     * Reconfigures the object if it's been instantiated.
     *
     * This reconfiguration doesn't affect fields with default values that were not
     * overridden in the {@link PropertySheet}, as those are set in the constructor.
     * Therefore it might have odd behaviour if there are default values.
     */
    public void reconfigure() {
        if (owner != null) {
            try {
                setConfiguredFields(owner, this);
            } catch (IllegalAccessException e) {
                throw new PropertyException(e,instanceName,"","Failed to reconfigure object");
            }
            try {
                owner.postConfig();
            } catch (IOException e) {
                throw new PropertyException(e, instanceName, null, "IOException thrown by postConfig");
            }
        }
    }

    protected void clearOwner() {
        owner = null;
    }

    /**
     * Returns the class of the owner configurable of this property sheet.
     */
    public Class<? extends Configurable> getConfigurableClass() {
        return ownerClass;
    }

    /**
     * Sets the raw property to the given name
     *
     * @param key the simple property name
     * @param val the value for the property
     */
    public void setProp(String key, Object val) {
        // ensure that there is such a property
        if (!registeredProperties.keySet().contains(key)) {
            throw new PropertyException(instanceName, "","'" + key + "' is not a registered property");
        }

        rawProps.put(key, val);
        propValues.put(key, val);

        if (instanceName != null) {
            cm.fireConfChanged(instanceName, key);
        }

        if (owner != null) {
            reconfigure();
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
        if (annotation instanceof Config) {
            return PropertyType.CONFIG;
        } else if (annotation instanceof ConfigurableName) {
            return PropertyType.COMPNAME;
        } else if (annotation instanceof ConfigManager) {
            return PropertyType.CONMAN;
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
            Object o = rawProps.get(regProp);
            if (o instanceof List) {
                ps.rawProps.put(regProp, new ArrayList<String>((List<? extends String>) o));
                ps.propValues.put(regProp, null);
            } else if (o instanceof Map) {
                ps.rawProps.put(regProp, new HashMap<String,String>((Map<String,String>) o));
                ps.propValues.put(regProp, null);
            }
        }

        ps.cm = cm;
        ps.owner = null;
        ps.instanceName = this.instanceName;

        return ps;
    }

    /**
     * Gets all of the fields associated with a class by walking up the class
     * tree. Handles super classes, as well as interfaces.
     *
     * @param configurable the class who's fields we wish to walk.
     * @return all of the fields, so they can be checked for annotations.
     */
    private static Set<Field> getAllFields(Class configurable) {
        Set<Field> ret = new HashSet<>();
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(configurable);
        while (!cq.isEmpty()) {
            Class curr = cq.remove();
            ret.addAll(Arrays.asList(curr.getDeclaredFields()));
            ret.addAll(Arrays.asList(curr.getFields()));
            Class sc = curr.getSuperclass();
            if (sc != null) {
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
    public static void processAnnotations(PropertySheet propertySheet,
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
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | InstantiationException ex) {
                        throw new PropertyException(ex, propertySheet.instanceName, method.getName(),
                                "Error invoking configurable method: "
                                + method.getName());
                    }
                }
            }
        }

        //
        // The java version.
        Set<Field> classFields = getAllFields(configurable);

        for (Field field : classFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Config) {
                    //
                    // We have a variable annotated with the Config annotation.
                    propertySheet.registerProperty(field.getName(), new ConfigPropWrapper((Proxy) annotation));
                } else if (annotation instanceof ConfigurableName) {
                    if (!field.getType().equals(String.class)) {
                        throw new PropertyException(propertySheet.getInstanceName(),field.getName(),"The component name must be an instance of java.lang.String");
                    }
                } else if (annotation instanceof ConfigManager) {
                    if (!field.getType().equals(ConfigurationManager.class)) {
                        throw new PropertyException(propertySheet.getInstanceName(),field.getName(),"The ConfigManager field must be an instance of ConfigurationManager");
                    }
                }
            }
        }
    }

    protected void save(PrintWriter writer) {
        Collection<String> registeredProperties = getRegisteredProperties();
        if (registeredProperties.size() != 0) {
            writer.printf("\t<component name=\"%s\" type=\"%s\" export=\"%s\" "
                            + "import=\"%s\">",
                    instanceName,
                    getConfigurableClass().getName(),
                    isExportable(),
                    isImportable());

            for (String propName : registeredProperties) {
                if (getRawNoReplacement(propName) == null) {
                    continue;
                }  // if the property was not defined within the xml file
                Object val = getRawNoReplacement(propName);
                if (val instanceof List) {
                    //
                    // Must be a string or component list
                    writer.printf("\n\t\t<propertylist name=\"%s\">", propName);
                    for (Object o : (List) val) {
                        if (o instanceof Class) {
                            writer.printf("\n\t\t\t<type>%s</type>", ((Class) o).getName());
                        } else {
                            writer.printf("\n\t\t\t<item>%s</item>", o);
                        }
                    }
                    writer.print("\n\t\t</propertylist>");
                } else if (val instanceof Set) {
                    writer.printf("\n\t\t<propertylist name=\"%s\">", propName);
                    for (Object o : (Set) val) {
                        if (o instanceof Class) {
                            writer.printf("\n\t\t\t<type>%s</type>", ((Class) o).
                                    getName());
                        } else {
                            writer.printf("\n\t\t\t<item>%s</item>", o);
                        }
                    }
                    writer.print("\n\t\t</propertylist>");
                } else if (val instanceof Map) {
                    //
                    // Must be a string,string map
                    writer.printf("\n\t\t<propertymap name=\"%s\">", propName);
                    for (Map.Entry<String, String> e : ((Map<String, String>) val).entrySet()) {
                        writer.printf("\n\t\t\t<entry key=\"%s\" value=\"%s\"/>", e.getKey(), e.getValue());
                    }
                    writer.print("\n\t\t</propertymap>");
                } else {
                    //
                    // Standard property
                    writer.printf("\n\t\t<property name=\"%s\" value=\"%s\"/>",
                            propName,
                            getRawNoReplacement(propName));
                }
            }

            writer.println("\n\t</component>");
        } else {
            //
            // Must be a component, which doesn't have properties
            writer.printf("\t<component name=\"%s\" type=\"%s\" export=\"%s\" "
                            + "import=\"%s\"/>\n",
                    instanceName,
                    getConfigurableClass().getName(),
                    isExportable(),
                    isImportable());

        }

    }

}
