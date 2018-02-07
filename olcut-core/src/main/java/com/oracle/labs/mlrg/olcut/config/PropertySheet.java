package com.oracle.labs.mlrg.olcut.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.oracle.labs.mlrg.olcut.util.IOUtil;

/**
 * A property sheet which defines a collection of properties for a single
 * component in the system.
 */
public class PropertySheet<T extends Configurable> implements Cloneable {
    private static final Logger logger = Logger.getLogger(PropertySheet.class.getName());

    public enum PropertyType { CONFIG, COMPNAME, CONMAN; }

    private Map<String, Config> registeredProperties
            = new HashMap<String, Config>();

    private Map<String, Object> propValues = new HashMap<String, Object>();

    /**
     * Maps the names of the component properties to their (possibly unresolved)
     * values.
     * <p/>
     * Example: <code>frontend</code> to <code>${myFrontEnd}</code>
     */
    private Map<String, Object> rawProps = new HashMap<String, Object>();

    private Map<String, Object> flatProps;

    protected ConfigurationManager cm;

    protected T owner;

    private RawPropertyData rpd;

    private boolean exportable;

    private boolean importable;

    private String serializedForm;

    protected final Class<T> ownerClass;

    protected String instanceName;

    public PropertySheet(T configurable, String name,
                         ConfigurationManager cm, RawPropertyData rpd) {
        this((Class<T>)configurable.getClass(), name, cm, rpd);
        owner = configurable;
    }

    public PropertySheet(Class<T> confClass, String name,
            ConfigurationManager cm, RawPropertyData rpd) {
        ownerClass = confClass;
        this.cm = cm;
        this.instanceName = name;
        exportable = rpd.isExportable();
        importable = rpd.isImportable();
        serializedForm = rpd.getSerializedForm();

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

    public void setImportable(boolean importable) {
        this.importable = importable;
    }

    public boolean isImportable() {
        return importable;
    }

    public Set<String> getPropertyNames() {
        return new HashSet<>(rawProps.keySet());
    }

    /**
     * Registers a new property which type and default value are defined by the
     * given olcut property.
     *
     * @param propName The name of the property to be registered.
     * @param property The property annotation masked by a proxy.
     */
    private void registerProperty(String propName, Config property) {
        assert property != null && propName != null;

        registeredProperties.put(propName, property);
        propValues.put(propName, null);
        rawProps.put(propName, null);
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
    public T getOwner() {
        return getOwner(null);
    }

    /**
     * Gets the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     *
     * @param reuseComponent if <code>true</code>, a previously configured
     * component will be returned, if there is one. If <code>false</code> a
     * newly instantiated and configured component will be returned.
     */
    public synchronized T getOwner(boolean reuseComponent) {
        return getOwner(null, reuseComponent);
    }

    /**
     * Returns the owner of this property sheet. In most cases this will be the
     * configurable instance which was instrumented by this property sheet.
     */
    public synchronized T getOwner(ComponentListener<T> cl) {
        return getOwner(cl, true);
    }

    public synchronized T getOwner(ComponentListener<T> cl, boolean reuseComponent) {
        try {
            if (!isInstantiated() || !reuseComponent) {

                //
                // Should we load a serialized form?
                if (serializedForm != null) {
                    String actualLocation = cm.getGlobalProperties().replaceGlobalProperties(instanceName, null, serializedForm);
                    InputStream serStream = IOUtil.getInputStreamForLocation(actualLocation);
                    if (serStream != null) {
                        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024 * 1024))) {
                            Object deser = ois.readObject();
                            owner = ownerClass.cast(deser);
                            return owner;
                        } catch (IOException ex) {
                            throw new PropertyException(ex, instanceName, null,
                                    "Error reading serialized form from " + actualLocation);
                        } catch (ClassNotFoundException ex) {
                            throw new PropertyException(ex, instanceName, null,
                                    "Serialized class not found at " + actualLocation);
                        }
                    }
                }

                if (ownerClass.isInterface()) {
                    throw new PropertyException(instanceName,"Failed to lookup interface " + ownerClass.getName() + " in registry, or deserialise it.");
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
                    Constructor<T> constructor = ownerClass.getDeclaredConstructor();
                    boolean isAccessible = constructor.isAccessible();
                    constructor.setAccessible(true);
                    owner = constructor.newInstance();
                    constructor.setAccessible(isAccessible);
                } catch (NoSuchMethodException ex) {
                    throw new PropertyException(ex, instanceName, null,
                            "No-args constructor not found for class " + ownerClass);
                } catch (InvocationTargetException ex) {
                    throw new InternalConfigurationException(ex, instanceName, null,
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
                        throw new PropertyException(e, instanceName, null, null);
                    }
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
    @SuppressWarnings("unchecked")
    private static <T extends Configurable> void setConfiguredFields(T o, PropertySheet ps) throws PropertyException, IllegalAccessException {

        Class<? extends Configurable> curClass = o.getClass();
        Set<Field> fields = getAllFields(curClass);
        for (Field f : fields) {
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            Config configAnnotation = f.getAnnotation(Config.class);
            ConfigurableName nameAnnotation = f.getAnnotation(ConfigurableName.class);
            ConfigManager cmAnnotation = f.getAnnotation(ConfigManager.class);
            if (configAnnotation != null) {
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
                    if (configAnnotation.mandatory()) {
                        throw new PropertyException(ps.getInstanceName(), f.getName(), f.getName() + " is mandatory in configuration");
                    } else {
                        continue;
                    }
                }

                //
                // We'll handle things that have list or arrays with items separately.
                if (FieldType.arrayTypes.contains(ft)) {
                    List vals = (List) ps.propValues.get(f.getName());
                    f.set(o, parseArrayField(ps.getConfigurationManager(), ps.getInstanceName(), f.getName(), f.getType(), ft, vals));
                } else if (FieldType.listTypes.contains(ft)) {
                    List<Class<?>> genericList = getGenericClass(f);
                    if (genericList.size() == 1) {
                        List vals = (List) ps.propValues.get(f.getName());
                        f.set(o, parseListField(ps.getConfigurationManager(), ps.getInstanceName(), f.getName(), f.getType(), genericList.get(0), ft, vals));
                    } else {
                        f.setAccessible(accessible);
                        throw new PropertyException(ps.getInstanceName(), f.getName(), "Failed to extract generic type arguments from field. Found: " + genericList.toString());
                    }
                } else if (FieldType.simpleTypes.contains(ft)) {
                    //
                    // We'll use flattenProp so that we take care of any variables
                    // in the single value.
                    String val = ps.flattenProp(f.getName());
                    f.set(o, parseSimpleField(ps.getConfigurationManager(), ps.getInstanceName(), f.getName(), f.getType(), ft, val));
                } else if (FieldType.mapTypes.contains(ft)){
                    //
                    // Last option is a map, as it's not a single value or a list.
                    List<Class<?>> genericList = getGenericClass(f);
                    if (genericList.size() == 2) {
                        Map<String, String> mapVals = (Map<String, String>) ps.propValues.get(f.getName());
                        f.set(o, parseMapField(ps.getConfigurationManager(), ps.getInstanceName(), f.getName(), genericList.get(1), mapVals));
                    } else {
                        f.setAccessible(accessible);
                        throw new PropertyException(ps.getInstanceName(), f.getName(), "Failed to extract generic type arguments from field. Found: " + genericList.toString());
                    }
                } else {
                    f.setAccessible(accessible);
                    throw new PropertyException(ps.getInstanceName(), f.getName(), "Unknown field type " + ft.toString());
                }
            } else if (nameAnnotation != null) {
                if (String.class.isAssignableFrom(f.getType())) {
                    f.set(o, ps.getInstanceName());
                } else {
                    f.setAccessible(accessible);
                    throw new PropertyException(ps.getInstanceName(), f.getName(), "Assigning ConfigurableName to non-String type " + f.getType().getName());
                }
            } else if (cmAnnotation != null) {
                if (ConfigurationManager.class.isAssignableFrom(f.getType())) {
                    f.set(o, ps.getConfigurationManager());
                } else {
                    f.setAccessible(accessible);
                    throw new PropertyException(ps.getInstanceName(), f.getName(), "Assigning ConfigManager to non-ConfigurationManager type " + f.getType().getName());
                }
            }
            f.setAccessible(accessible);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map parseMapField(ConfigurationManager cm, String instanceName, String fieldName, Class<?> genericType, Map<String,String> input) {
        FieldType genericft = FieldType.getFieldType(genericType);
        Map map = new HashMap<>();
        for (Map.Entry<String, String> e : input.entrySet()) {
            String newVal = cm.getGlobalProperties().replaceGlobalProperties(instanceName, fieldName, e.getValue());
            map.put(e.getKey(), parseSimpleField(cm, instanceName, fieldName, genericType, genericft, newVal));
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Object parseArrayField(ConfigurationManager cm, String instanceName, String fieldName, Class<?> fieldClass, FieldType ft, List input) {
        //
        // This dance happens as some of the list types support class values,
        // and this is the first place where FieldType meets the value loaded
        // from the xml file, so we have to check it.
        List<String> replaced = new ArrayList<>();
        List<Class<?>> classVals = new ArrayList<>();
        List<Class<?>> removeList = new ArrayList<>();
        for (Object val : input) {
            if (val instanceof String) {
                replaced.add(cm.getGlobalProperties().replaceGlobalProperties(instanceName, fieldName, (String) val));
            } else if (val instanceof Class) {
                classVals.add((Class<?>) val);
            } else {
                throw new PropertyException(instanceName,fieldName,"Unknown type loaded from property, found " + val.getClass().getName());
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
                    Configurable c = cm.lookup(name);
                    if (c == null) {
                        throw new PropertyException(instanceName, fieldName, fieldName + " looked up an unknown configurable called " + name);
                    }
                    configurableList.add(c);
                }
                for (Class c : classVals) {
                    if (configArrayType.isAssignableFrom(c)) {
                        configurableList.addAll(cm.lookupAll(c, null));
                        removeList.add(c);
                    } else {
                        throw new PropertyException(instanceName, fieldName, "Unassignable class " + c.getName() + " to configArrayType " + configArrayType.getName());
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a byte", replaced.toString()));
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a short", replaced.toString()));
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not an integer", replaced.toString()));
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not an array of long", replaced.toString()));
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s does not specify an array of float", replaced.toString()));
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
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s does not specify an array of double", replaced.toString()));
                }
                break;
        }
        classVals.removeAll(removeList);
        if (classVals.size() > 0) {
            throw new PropertyException(instanceName,fieldName,"Found class values in a primitive array");
        }
        return output;
    }
    @SuppressWarnings("unchecked")
    public static Object parseListField(ConfigurationManager cm, String instanceName, String fieldName, Class<?> fieldClass, Class<?> genericClass, FieldType ft, List input) {
        //
        // This dance happens as some of the list types support class values,
        // and this is the first place where FieldType meets the value loaded
        // from the xml file, so we have to check it.
        List<String> replaced = new ArrayList<>();
        List<Class<?>> classVals = new ArrayList<>();
        List<Class<?>> removeList = new ArrayList<>();
        for (Object val : input) {
            if (val instanceof String) {
                replaced.add(cm.getGlobalProperties().replaceGlobalProperties(instanceName, fieldName, (String) val));
            } else if (val instanceof Class) {
                classVals.add((Class<?>) val);
            } else {
                throw new PropertyException(instanceName,fieldName,"Unknown type loaded from property, found " + val.getClass().getName());
            }
        }

        //
        // Now go through the valid list types and assign the output field.
        Object output = null;
        FieldType genericft;
        switch (ft) {
            case ENUM_SET:
                try {
                    Class<Enum> enumType = (Class<Enum>) genericClass;
                    EnumSet s = EnumSet.noneOf(enumType);
                    for (String v : replaced) {
                        s.add(Enum.valueOf(enumType, v.toUpperCase()));
                    }
                    output = s;
                } catch (ClassCastException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("The supplied type %s is not an Enum type", genericClass.getName()));
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s has values not in %s", replaced.toString(), fieldClass));
                }
                break;
            case LIST:
                genericft = FieldType.getFieldType(genericClass);
                List list = new ArrayList(replaced.size());
                for (String v : replaced) {
                    list.add(parseSimpleField(cm, instanceName, fieldName, genericClass, genericft, v));
                }
                for (Class c : classVals) {
                    if (genericClass.isAssignableFrom(c)) {
                        list.addAll(cm.lookupAll(c, null));
                        removeList.add(c);
                    } else {
                        throw new PropertyException(instanceName, fieldName, "Unassignable class " + c.getName() + " to genericType " + genericClass.getName());
                    }
                }
                output = list;
                break;
            case SET:
                genericft = FieldType.getFieldType(genericClass);
                Set set = new HashSet(replaced.size());
                for (String v : replaced) {
                    set.add(parseSimpleField(cm, instanceName, fieldName, genericClass, genericft, v));
                }
                for (Class c : classVals) {
                    if (genericClass.isAssignableFrom(c)) {
                        set.addAll(cm.lookupAll(c, null));
                        removeList.add(c);
                    } else {
                        throw new PropertyException(instanceName, fieldName, "Unassignable class " + c.getName() + " to genericClass " + genericClass.getName());
                    }
                }
                output = set;
                break;
        }
        classVals.removeAll(removeList);
        if (classVals.size() > 0) {
            throw new PropertyException(instanceName,fieldName,"Found class values in a primitive array");
        }
        return output;
    }

    public static Object parseSimpleField(ConfigurationManager cm, String instanceName, String fieldName, Class<?> fieldClass, FieldType ft, String val) {
        switch (ft) {
            case STRING:
                return val;
            case BOOLEAN:
                return Boolean.parseBoolean(val);
            case BYTE:
                try {
                    return Byte.parseByte(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a byte", val));
                }
            case SHORT:
                try {
                    return Short.parseShort(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a short", val));
                }
            case INTEGER:
                try {
                    return Integer.parseInt(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not an integer", val));
                }
            case ATOMIC_INTEGER:
                try {
                    return new AtomicInteger(Integer.parseInt(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not an integer", val));
                }
            case LONG:
                try {
                    return Long.parseLong(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a long", val));
                }
            case ATOMIC_LONG:
                try {
                    return new AtomicLong(Long.parseLong(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a long", val));
                }
            case FLOAT:
                try {
                    return Float.parseFloat(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not an float", val));
                }
            case DOUBLE:
                try {
                    return Double.parseDouble(val);
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a double", val));
                }
            case FILE:
                return new File(val);
            case PATH:
                return Paths.get(val);
            case RANDOM:
                try {
                    return new Random(Integer.parseInt(val));
                } catch (NumberFormatException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("The seed %s is not an integer", val));
                }
            case ENUM:
                try {
                    return Enum.valueOf((Class<Enum>) fieldClass, val);
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException(ex, instanceName, fieldName, String.format("%s is not a value of %s", val, fieldClass));
                }
            case CONFIGURABLE:
                Configurable comp = cm.lookup(val);
                if (comp == null) {
                    throw new PropertyException(instanceName, fieldName, fieldName + " looked up an unknown component called " + val);
                }
                return comp;
            default:
                throw new PropertyException(instanceName, fieldName, fieldName + " was not a simple configurable field");
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
                    List<Class<?>> genericList = getGenericClass(f);
                    if (FieldType.arrayTypes.contains(ft)) {
                        return parseArrayField(getConfigurationManager(), instanceName, f.getName(), f.getType(), ft, (List) propValues.get(f.getName()));
                    } else if (FieldType.listTypes.contains(ft)) {
                        if (genericList.size() == 1) {
                            return parseListField(getConfigurationManager(), instanceName, f.getName(), f.getType(), genericList.get(0), ft, (List) propValues.get(f.getName()));
                        } else {
                            throw new PropertyException(getInstanceName(), f.getName(), "Failed to extract generic type arguments from field. Found: " + genericList.toString());
                        }
                    } else if (FieldType.simpleTypes.contains(ft)) {
                        return parseSimpleField(getConfigurationManager(), instanceName, f.getName(),f.getType(),ft, flattenProp(f.getName()));
                    } else {
                        if (genericList.size() == 2) {
                            return parseMapField(getConfigurationManager(), instanceName, f.getName(), genericList.get(1), (Map<String, String>) propValues.get(f.getName()));
                        } else {
                            throw new PropertyException(getInstanceName(), f.getName(), "Failed to extract generic type arguments from field. Found: " + genericList.toString());
                        }
                    }
                }
            }

        }
        return null;
    }

    /**
     * Extracts the classes representing the generic type parameters for the supplied field.
     *
     * It ignores types which aren't classes. If you've got those, you're on your own.
     *
     * @param f The field to inspect.
     * @return A list of classes representing the generic types.
     */
    public static List<Class<?>> getGenericClass(Field f) {
        List<Class<?>> list = new ArrayList<>();

        Type genericType = f.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            for (Type t : pt.getActualTypeArguments()) {
                if (t instanceof Class) {
                    list.add((Class<?>)t);
                }
            }
        }

        return list;
    }

    public void clearOwner() {
        owner = null;
    }

    /**
     * Returns the class of the owner configurable of this property sheet.
     */
    public Class<T> getConfigurableClass() {
        return ownerClass;
    }

    /**
     * Sets the raw property to the given name.
     *
     * If the owner is instantiated it *does not* change the field in the owner.
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
     * @return the value as an object (it could be a String, a List, or a Map
     * depending upon the property type)
     */
    public Object getRaw(String name) {
        return rawProps.get(name);
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
        PropertySheet<T> ps = (PropertySheet<T>) super.clone();

        ps.registeredProperties = new HashMap<>(this.registeredProperties);
        ps.propValues = new HashMap<>(this.propValues);

        ps.rawProps = new HashMap<>(this.rawProps);

        // make deep copy of raw-lists
        for (String regProp : ps.getRegisteredProperties()) {
            Object o = rawProps.get(regProp);
            if (o instanceof List) {
                ps.rawProps.put(regProp, new ArrayList<Object>((List) o));
                ps.propValues.put(regProp, null);
            } else if (o instanceof Map) {
                ps.rawProps.put(regProp, new HashMap<>((Map<String,String>) o));
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
    public static Set<Field> getAllFields(Class<? extends Configurable> configurable) {
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
     * @param configurable of type <code>Class&lt;? extends Configurable&gt;</code>
     */
    public static <T extends Configurable> void processAnnotations(PropertySheet<T> propertySheet,
            Class<T> configurable) throws PropertyException {
        Set<Field> classFields = getAllFields(configurable);

        for (Field field : classFields) {
            Config configAnnotation = field.getAnnotation(Config.class);
            ConfigurableName nameAnnotation = field.getAnnotation(ConfigurableName.class);
            ConfigManager cmAnnotation = field.getAnnotation(ConfigManager.class);
            if (((configAnnotation != null) && (nameAnnotation != null)) || ((nameAnnotation != null) && (cmAnnotation != null)) || ((configAnnotation != null) && (cmAnnotation != null))) {
                throw new PropertyException(propertySheet.getInstanceName(), field.getName(), "Multiple olcut annotations applied to the same field");
            }
            if (configAnnotation != null) {
                //
                // We have a variable annotated with the Config annotation.
                propertySheet.registerProperty(field.getName(), configAnnotation);
            } else if (nameAnnotation != null) {
                if (!field.getType().equals(String.class)) {
                    throw new PropertyException(propertySheet.getInstanceName(),field.getName(),"The component name must be an instance of java.lang.String");
                }
            } else if (cmAnnotation != null) {
                if (!field.getType().equals(ConfigurationManager.class)) {
                    throw new PropertyException(propertySheet.getInstanceName(),field.getName(),"The ConfigManager field must be an instance of ConfigurationManager");
                }
            }
        }
    }

    public void save(ConfigWriter configWriter) throws ConfigWriterException {
        Collection<String> registeredProperties = getRegisteredProperties();
        Map<String,String> attributes = new HashMap<>();
        if (registeredProperties.size() > 0) {
            configWriter.writeStartElement("component");
            configWriter.writeAttribute("name", instanceName);
            configWriter.writeAttribute("type", getConfigurableClass().getName());
            configWriter.writeAttribute("export", "" + isExportable());
            configWriter.writeAttribute("import", "" + isImportable());
            configWriter.writeRaw(System.lineSeparator());

            configWriter.writeArrayStart("properties");
            for (String propName : registeredProperties) {
                if (getRaw(propName) == null) {
                    continue;
                }  // if the property was not defined within the xml file
                Object val = getRaw(propName);
                if ((val instanceof List)) {
                    //
                    // Must be a string or component list
                    configWriter.writeRaw("\t");
                    configWriter.writeStartElement("propertylist");
                    configWriter.writeAttribute("name",propName);
                    configWriter.writeRaw(System.lineSeparator());
                    configWriter.writeArrayStart("list");
                    for (Object o : (List) val) {
                        if (o instanceof Class) {
                            configWriter.writeRaw("\t\t");
                            configWriter.writeStartElement("type");
                            configWriter.writeMember(((Class) o).getName());
                            configWriter.writeEndElement();
                            configWriter.writeRaw(System.lineSeparator());
                        } else {
                            configWriter.writeRaw("\t\t");
                            configWriter.writeStartElement("item");
                            configWriter.writeMember(o.toString());
                            configWriter.writeEndElement();
                            configWriter.writeRaw(System.lineSeparator());
                        }
                    }
                    configWriter.writeArrayEnd();
                    configWriter.writeRaw("\t");
                    configWriter.writeEndElement();
                    configWriter.writeRaw(System.lineSeparator());
                } else if (val instanceof Map) {
                    //
                    // Must be a string,string map
                    configWriter.writeRaw("\t");
                    configWriter.writeStartElement("propertymap");
                    configWriter.writeAttribute("name",propName);
                    configWriter.writeRaw(System.lineSeparator());
                    configWriter.writeArrayStart("list");
                    for (Map.Entry<String, String> e : ((Map<String, String>) val).entrySet()) {
                        configWriter.writeRaw("\t\t");
                        attributes.clear();
                        attributes.put("key",e.getKey());
                        attributes.put("value",e.getValue());
                        configWriter.writeElement("entry",attributes);
                        configWriter.writeRaw(System.lineSeparator());
                    }
                    configWriter.writeArrayEnd();
                    configWriter.writeRaw("\t");
                    configWriter.writeEndElement();
                    configWriter.writeRaw(System.lineSeparator());
                } else {
                    //
                    // Standard property
                    configWriter.writeRaw("\t");
                    attributes.clear();
                    attributes.put("name",propName);
                    attributes.put("value",getRaw(propName).toString());
                    configWriter.writeElement("property",attributes);
                    configWriter.writeRaw(System.lineSeparator());
                }
            }
            configWriter.writeArrayEnd();

            configWriter.writeEndElement();
            configWriter.writeRaw(System.lineSeparator());
        } else {
            attributes.clear();
            attributes.put("name",instanceName);
            attributes.put("type",getConfigurableClass().getName());
            attributes.put("export",""+isExportable());
            attributes.put("import",""+isImportable());
            configWriter.writeRaw(System.lineSeparator());
        }
    }

}
