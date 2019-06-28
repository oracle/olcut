package com.oracle.labs.mlrg.olcut.provenance.impl;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.FieldType;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.MapProvenance;
import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.BooleanProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ByteProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.CharProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.EnumProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FloatProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.LongProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ShortProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.URIProvenance;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A pile of reflection based magic used to automatically extract the values of configurable
 * fields. Supports all the types used by the configuration system, except for
 * Random as it's impossible to generate a true provenance for a {@link java.util.Random} instance.
 *
 * This class can be used as the basis for ConfiguredObjectProvenance implementations, it
 * automatically extracts any configured fields from the host object and stores the values
 * in the appropriate provenance type.
 *
 * It is recommended that subclasses of this class implement a static method which accepts
 * a <code>Map&lt;String,Provenance&gt;</code> and returns a {@link ExtractedInfo}.
 * As with all subclasses of {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance}
 * it is required that they expose a public constructor which accepts a <code>Map&lt;String,Provenance&gt;</code>.
 */
public abstract class SkeletalConfiguredObjectProvenance implements ConfiguredObjectProvenance {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(SkeletalConfiguredObjectProvenance.class.getName());

    protected static final String HOST_SHORT_NAME = "host-short-name";

    protected final String className;
    protected final String hostShortName;
    protected final Map<String, Provenance> configuredParameters;

    /**
     * This constructor is used to construct a provenance automatically by inspecting the configurable
     * fields of the host object.
     * @param host The object to inspect.
     * @param hostShortName The name to supply in the toString.
     * @param <T> The type of the host object.
     */
    protected <T extends Configurable> SkeletalConfiguredObjectProvenance(T host, String hostShortName) {
        this.className = host.getClass().getName();
        this.hostShortName = hostShortName;
        Map<String,Provenance> provMap = AccessController.doPrivileged((PrivilegedAction<Map<String,Provenance>>)() -> getConfiguredFields(host));
        this.configuredParameters = Collections.unmodifiableMap(provMap);
    }

    /**
     * This constructor is used when deserialising an ObjectMarshalledProvenance, which supplies a
     * <code>Map&lt;String,Provenance&gt;</code>. This Map must be further transformed to split out
     * the configured parameters from the instance values, class name and hostShortName.
     *
     * It's currently not possible to enforce that a method which transforms the Map into an {@link ExtractedInfo}
     * exists, as the method must be both static and specific to the subclass. Hopefully this will
     * be enforced in a future release depending on a future JDK.
     * @param info The provenance information.
     */
    protected SkeletalConfiguredObjectProvenance(ExtractedInfo info) {
        this.className = info.className;
        this.hostShortName = info.hostShortName;
        this.configuredParameters = info.configuredParameters;
    }

    /**
     * This is a carrier class for the provenance information. It
     * separates out the class name, hostShortName, configured parameters
     * and instance parameters. It's used to provide the appropriate information
     * to the {@link SkeletalConfiguredObjectProvenance#SkeletalConfiguredObjectProvenance(ExtractedInfo)}
     * constructor to prevent that constructor from triggering class loading of the
     * host class.
     *
     * The class loading would be required to separate out the configured parameters from the instance
     * values, however subclasses of {@link SkeletalConfiguredObjectProvenance} must supply it
     * themselves via an extraction method.
     */
    protected static class ExtractedInfo {
        public final String className;
        public final String hostShortName;
        public final Map<String,Provenance> configuredParameters;
        public final Map<String,PrimitiveProvenance<?>> instanceValues;

        public ExtractedInfo(String className, String hostShortName, Map<String,Provenance> configuredParameters, Map<String,PrimitiveProvenance<?>> instanceValues) {
            this.className = className;
            this.hostShortName = hostShortName;
            this.configuredParameters = configuredParameters;
            this.instanceValues = instanceValues;
        }
    }

    private static <T extends Configurable> Map<String, Provenance> getConfiguredFields(T host) {
        Map<String, Provenance> map = new HashMap<>();
        Class<? extends Configurable> hostClass = host.getClass();
        Set<Field> fields = PropertySheet.getAllFields(hostClass);
        try {
            for (Field f : fields) {
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                // if configurable
                if (f.isAnnotationPresent(Config.class)) {
                    FieldType ft = FieldType.getFieldType(f);
                    switch (ft) {
                        case BOOLEAN:
                        case BYTE:
                        case CHAR:
                        case SHORT:
                        case INTEGER:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                        case STRING:
                        case FILE:
                        case PATH:
                        case ENUM:
                        case CONFIGURABLE:
                        case ATOMIC_INTEGER:
                        case ATOMIC_LONG:
                            Optional<Provenance> opt = convertPrimitive(ft,f.getType(),f.getName(),f.get(host));
                            if (opt.isPresent()) {
                                map.put(f.getName(), opt.get());
                            }
                            break;
                        case BYTE_ARRAY:
                        case CHAR_ARRAY:
                        case SHORT_ARRAY:
                        case INTEGER_ARRAY:
                        case LONG_ARRAY:
                        case FLOAT_ARRAY:
                        case DOUBLE_ARRAY:
                            map.put(f.getName(), convertPrimitiveArray(ft, f, f.get(host)));
                            break;
                        case STRING_ARRAY:
                        case CONFIGURABLE_ARRAY:
                            map.put(f.getName(), convertObjectArray(ft, f, (Object[]) f.get(host)));
                            break;
                        case LIST:
                        case ENUM_SET:
                        case SET: {
                            List<Class<?>> genericClasses = PropertySheet.getGenericClass(f);
                            if (genericClasses.size() != 1) {
                                logger.log(Level.SEVERE, "Invalid configurable field definition, field not recorded - found too many or too few generic type parameters for field " + f.getName());
                            } else {
                                map.put(f.getName(), convertCollection(f, (Collection) f.get(host), genericClasses.get(0)));
                            }
                            break;
                        }
                        case MAP: {
                            List<Class<?>> genericClasses = PropertySheet.getGenericClass(f);
                            if (genericClasses.size() != 2) {
                                logger.log(Level.SEVERE, "Invalid configurable field definition, field not recorded - found too many or too few generic type parameters for field " + f.getName());
                            } else {
                                map.put(f.getName(), convertMap(f, (Map) f.get(host), genericClasses.get(1)));
                            }
                            break;
                        }
                        case RANDOM:
                        default:
                            logger.log(Level.SEVERE, "Automatic provenance not supported for field type " + ft + ", field '" + f.getName() + "' not recorded.");
                            break;
                    }
                }
                f.setAccessible(accessible);
            }
        } catch (ClassCastException e) {
            logger.log(Level.SEVERE, "Failed to cast field from host object " + host.toString() + ". Fields not recorded.", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to access field in host object " + host.toString() + ". Fields not recorded.", e);
        }
        return map;
    }

    private static ListProvenance convertPrimitiveArray(FieldType ft, Field f, Object object) {
        String fieldName = f.getName();
        ArrayList<PrimitiveProvenance> list = new ArrayList<>();
        switch (ft) {
            case BYTE_ARRAY: {
                byte[] array = (byte[]) object;
                for (byte e : array) {
                    list.add(new ByteProvenance(fieldName,e));
                }
                break;
            }
            case CHAR_ARRAY:{
                char[] array = (char[]) object;
                for (char e : array) {
                    list.add(new CharProvenance(fieldName,e));
                }
                break;
            }
            case SHORT_ARRAY:{
                short[] array = (short[]) object;
                for (short e : array) {
                    list.add(new ShortProvenance(fieldName,e));
                }
                break;
            }
            case INTEGER_ARRAY:{
                int[] array = (int[]) object;
                for (int e : array) {
                    list.add(new IntProvenance(fieldName,e));
                }
                break;
            }
            case LONG_ARRAY:{
                long[] array = (long[]) object;
                for (long e : array) {
                    list.add(new LongProvenance(fieldName,e));
                }
                break;
            }
            case FLOAT_ARRAY:{
                float[] array = (float[]) object;
                for (float e : array) {
                    list.add(new FloatProvenance(fieldName,e));
                }
                break;
            }
            case DOUBLE_ARRAY:{
                double[] array = (double[]) object;
                for (double e : array) {
                    list.add(new DoubleProvenance(fieldName,e));
                }
                break;
            }
            default:
                logger.log(Level.SEVERE, "Automatic provenance not supported for field type " + ft + ", field '" + f.getName() + "' not recorded.");
                return new ListProvenance<>();
        }
        return new ListProvenance<>(list);
    }

    private static ListProvenance convertObjectArray(FieldType ft, Field f, Object[] array) {
        String fieldName = f.getName();
        switch (ft) {
            case STRING_ARRAY:
                List<StringProvenance> sp = new ArrayList<>();
                for (Object o : array) {
                    String s = (String) o;
                    sp.add(new StringProvenance(fieldName,s));
                }
                return new ListProvenance<>(sp);
            case CONFIGURABLE_ARRAY:
                List<Provenance> lp = new ArrayList<>();
                for (Object o : array) {
                    if (o == null) {
                        lp.add(ConfiguredObjectProvenance.getEmptyProvenance(f.getType().getComponentType().getName()));
                    } else if (o instanceof Provenancable) {
                        Provenancable p = (Provenancable) o;
                        lp.add(p.getProvenance());
                    } else {
                        logger.log(Level.WARNING, "Automatic provenance generated for Configurable class, consider opting into provenance by implementing Provenancable on class " + o.getClass().toString());
                        lp.add(new ConfiguredObjectProvenanceImpl((Configurable)o, fieldName));
                    }
                }
                return new ListProvenance<>(lp);
            default:
                logger.log(Level.SEVERE, "Automatic provenance not supported for field type " + ft + ", field '" + f.getName() + "' not recorded.");
                return new ListProvenance();
        }
    }

    private static ListProvenance convertCollection(Field f, Collection collection, Class<?> genericType) {
        String fieldName = f.getName();
        FieldType genericFieldType = FieldType.getFieldType(genericType);
        List<Provenance> list = new ArrayList<>();

        for (Object o : collection) {
            Optional<Provenance> opt = convertPrimitive(genericFieldType,genericType,fieldName,o);
            opt.ifPresent(list::add);
        }

        return new ListProvenance<>(list);
    }

    private static MapProvenance convertMap(Field f, Map<?,?> inputMap, Class<?> genericType) {
        FieldType genericFieldType = FieldType.getFieldType(genericType);
        Map<String,Provenance> outputMap = new HashMap<>();

        for (Map.Entry<?,?> e : inputMap.entrySet()) {
            String key = e.getKey().toString();
            Optional<Provenance> opt = convertPrimitive(genericFieldType,genericType,key,e.getValue());
            if (opt.isPresent()) {
                outputMap.put(key,opt.get());
            }
        }

        return new MapProvenance<>(outputMap);
    }

    private static Optional<Provenance> convertPrimitive(FieldType ft, Class<?> fieldClass, String fieldName, Object o) {
        switch (ft) {
            case BOOLEAN:
                return Optional.of(new BooleanProvenance(fieldName, (Boolean) o));
            case BYTE:
                return Optional.of(new ByteProvenance(fieldName, (Byte) o));
            case CHAR:
                return Optional.of(new CharProvenance(fieldName, (Character) o));
            case SHORT:
                return Optional.of(new ShortProvenance(fieldName, (Short) o));
            case INTEGER:
                return Optional.of(new IntProvenance(fieldName, (Integer) o));
            case LONG:
                return Optional.of(new LongProvenance(fieldName, (Long) o));
            case FLOAT:
                return Optional.of(new FloatProvenance(fieldName, (Float) o));
            case DOUBLE:
                return Optional.of(new DoubleProvenance(fieldName, (Double) o));
            case STRING:
                return Optional.of(new StringProvenance(fieldName, (String) o));
            case FILE:
                return Optional.of(new URIProvenance(fieldName, ((File) o).toURI()));
            case PATH:
                return Optional.of(new URIProvenance(fieldName, ((Path) o).toUri()));
            case ENUM:
                return Optional.of(new EnumProvenance<>(fieldName, (Enum) o));
            case CONFIGURABLE:
                if (o == null) {
                    return Optional.of(ConfiguredObjectProvenance.getEmptyProvenance(fieldClass.getName()));
                } else if (o instanceof Provenancable) {
                    return Optional.of(((Provenancable) o).getProvenance());
                } else {
                    logger.log(Level.WARNING, "Automatic provenance generated for Configurable class, consider opting into provenance by implementing Provenancable on class " + o.getClass().toString());
                    return Optional.of(new ConfiguredObjectProvenanceImpl((Configurable)o, fieldName));
                }
            case ATOMIC_INTEGER:
                return Optional.of(new IntProvenance(fieldName, ((AtomicInteger) o).get()));
            case ATOMIC_LONG:
                return Optional.of(new LongProvenance(fieldName, ((AtomicLong) o).get()));
            case RANDOM:
                logger.log(Level.SEVERE, "Automatic provenance not supported for field type " + ft + ", field '" + fieldName + "' not recorded.");
                return Optional.empty();
            case BYTE_ARRAY:
            case CHAR_ARRAY:
            case SHORT_ARRAY:
            case INTEGER_ARRAY:
            case LONG_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case STRING_ARRAY:
            case CONFIGURABLE_ARRAY:
            case LIST:
            case ENUM_SET:
            case SET:
            case MAP:
            default:
                logger.log(Level.SEVERE, "Automatic provenance not supported for nested field type " + ft + ", field '" + fieldName + "' not recorded.");
                return Optional.empty();
        }
    }

    @Override
    public Map<String, PrimitiveProvenance<?>> getInstanceValues() {
        return Collections.singletonMap(HOST_SHORT_NAME, new StringProvenance(HOST_SHORT_NAME, hostShortName));
    }

    @Override
    public Map<String, Provenance> getConfiguredParameters() {
        return configuredParameters;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return generateString(hostShortName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfiguredObjectProvenanceImpl)) return false;
        ConfiguredObjectProvenanceImpl pairs = (ConfiguredObjectProvenanceImpl) o;
        return className.equals(pairs.className) &&
                hostShortName.equals(pairs.hostShortName) &&
                configuredParameters.equals(pairs.configuredParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, hostShortName, configuredParameters);
    }
}
