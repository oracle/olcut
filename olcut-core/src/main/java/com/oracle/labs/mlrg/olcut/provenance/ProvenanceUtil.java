package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.provenance.io.FlatMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ListMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.MapMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.SimpleMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.EnumProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.HashProvenance;
import com.oracle.labs.mlrg.olcut.util.IOUtil;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static utilities and helpers for working with Provenance objects.
 */
public final class ProvenanceUtil {

    private static final Logger logger = Logger.getLogger(ProvenanceUtil.class.getName());

    public enum HashType {
        SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512"), MD5("MD5");

        public final String name;
        HashType(String name) {
            this.name = name;
        }
    }

    private ProvenanceUtil(){}

    /**
     * Used to convert byte arrays into hex strings.
     */
    private final static char[] hexCharacterArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Converts a byte array into a hexadecimal encoded String.
     *
     * Used to convert message digests into Strings.
     *
     * The java.xml.bind.DataTypeConverter class was removed in Java 11, so this is a cross version replacement.
     * @param bytes The byte array to convert
     * @return A hexadecimal representation of the byte array.
     */
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j << 1] = hexCharacterArray[v >>> 4];
            hexChars[(j << 1) + 1] = hexCharacterArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hashList(HashType hashType, List<String> list) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashType.name);
            for (String curString : list) {
                md.digest(curString.getBytes(StandardCharsets.UTF_8));
            }
            return bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE,"Failed to load standard hash algorithm " + hashType.name);
            return "invalid-algorithm-specified";
        }
    }

    public static String hashResource(HashType hashType, Path path) {
        return hashResource(hashType,path.toFile());
    }

    public static String hashResource(HashType hashType, File file) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashType.name);
            byte[] buffer = new byte[16384];
            int count;
            try (InputStream bis = IOUtil.getInputStream(file)) {
                while ((count = bis.read(buffer)) > 0) {
                    md.update(buffer, 0, count);
                }
                return bytesToHexString(md.digest());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException when reading from file " + file);
                return bytesToHexString(md.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE,"Failed to load standard hash algorithm " + hashType.name);
            return "invalid-algorithm-specified";
        }
    }

    public static String hashResource(HashType hashType, URI file) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashType.name);
            byte[] buffer = new byte[16384];
            int count;
            try (InputStream bis = new BufferedInputStream(file.toURL().openStream())) {
                while ((count = bis.read(buffer)) > 0) {
                    md.update(buffer, 0, count);
                }
                return bytesToHexString(md.digest());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException when reading from file " + file);
                return bytesToHexString(md.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE,"Failed to load standard hash algorithm " + hashType.name);
            return "invalid-algorithm-specified";
        }
    }

    /**
     * Extracts a list of ConfigurationData which can be used to reconstruct the objects
     * recorded in this provenance.
     *
     * The configurations are given machine generated names, and it makes a best effort
     * attempt to flatten cycles without duplicating objects.
     * @param provenance The provenance to extract configuration from.
     * @return A list of configurations.
     */
    public static List<ConfigurationData> extractConfiguration(ObjectProvenance provenance) {
        Map<ConfiguredObjectProvenance,Integer> provenanceTracker = new IdentityHashMap<>(30);

        int counter = 0;

        // Extract all the ObjectProvenance instances from the object graph rooted at provenance
        Queue<ObjectProvenance> processingQueue = new LinkedList<>();
        processingQueue.add(provenance);
        while (!processingQueue.isEmpty()) {
            ObjectProvenance curProv = processingQueue.poll();
            if (curProv instanceof ConfiguredObjectProvenance) {
                provenanceTracker.put((ConfiguredObjectProvenance)curProv,counter);
                counter++;
            }
            extractProvenanceToQueue(processingQueue, curProv);
        }

        List<ConfigurationData> output = new ArrayList<>();

        for (Map.Entry<ConfiguredObjectProvenance,Integer> e : provenanceTracker.entrySet()) {
            output.add(extractSingleConfiguration(e.getKey(),computeName(e.getKey(),e.getValue()),provenanceTracker));
        }

        return output;
    }

    private static ConfigurationData extractSingleConfiguration(ConfiguredObjectProvenance obj, String objName, Map<ConfiguredObjectProvenance,Integer> map) {
        ConfigurationData data = new ConfigurationData(objName,obj.getClassName());

        for (Map.Entry<String,Provenance> e : obj.getConfiguredParameters().entrySet()) {
            Provenance prov = e.getValue();
            if (prov instanceof ListProvenance) {
                List<SimpleProperty> list = new ArrayList<>();

                for (Provenance p : (ListProvenance<?>)prov) {
                   if (p instanceof ConfiguredObjectProvenance) {
                        list.add(new SimpleProperty(computeName((ConfiguredObjectProvenance)p,map.get(p))));
                    } else {
                        list.add(new SimpleProperty(p.toString()));
                    }
                }

                data.add(e.getKey(),new ListProperty(list));
            } else if (prov instanceof MapProvenance) {
                Map<String,SimpleProperty> propMap = new HashMap<>();

                for (Pair<String,? extends Provenance> pair : (MapProvenance<?>)prov) {
                    Provenance valueProv = pair.getB();
                    if (valueProv instanceof ConfiguredObjectProvenance) {
                        propMap.put(pair.getA(),new SimpleProperty(computeName((ConfiguredObjectProvenance)valueProv,map.get(valueProv))));
                    } else {
                        propMap.put(pair.getA(),new SimpleProperty(valueProv.toString()));
                    }
                }

                data.add(e.getKey(),new MapProperty(propMap));
            } else if (prov instanceof ConfiguredObjectProvenance) {
                data.add(e.getKey(),new SimpleProperty(computeName((ConfiguredObjectProvenance)prov,map.get(prov))));
            } else {
                data.add(e.getKey(),new SimpleProperty(prov.toString()));
            }
        }

        return data;
    }

    /**
     * Creates a name for a provenance object on it's host class's simple name and the supplied
     * id number.
     * @param obj The provenance object.
     * @param number The id number.
     * @return A String name.
     */
    public static String computeName(ObjectProvenance obj, int number) {
        String className = obj.getClassName();
        int lastDot = className.lastIndexOf(".");
        if (lastDot != -1) {
            className = className.substring(lastDot+1);
        }
        return className.toLowerCase() + "-" + number;
    }

    public static List<ObjectMarshalledProvenance> marshalProvenance(ObjectProvenance provenance) {
        Map<ObjectProvenance,Integer> provenanceTracker = new LinkedHashMap<>();

        int counter = 0;

        // Extract all the ObjectProvenance instances from the object graph rooted at provenance
        Queue<ObjectProvenance> processingQueue = new LinkedList<>();
        processingQueue.add(provenance);
        while (!processingQueue.isEmpty()) {
            ObjectProvenance curProv = processingQueue.poll();
            provenanceTracker.put(curProv,counter);
            counter++;
            extractProvenanceToQueue(processingQueue, curProv);
        }

        List<ObjectMarshalledProvenance> output = new ArrayList<>();

        // Marshall each provenance into a flat representation
        for (Map.Entry<ObjectProvenance,Integer> e : provenanceTracker.entrySet()) {
            output.add(marshalSingleProvenance(e.getKey(),computeName(e.getKey(),e.getValue()),provenanceTracker));
        }

        return output;
    }

    private static ObjectMarshalledProvenance marshalSingleProvenance(ObjectProvenance provenance, String name, Map<ObjectProvenance,Integer> map) {
        Map<String, FlatMarshalledProvenance> outputMap = new HashMap<>();

        for (Pair<String,Provenance> e : provenance) {
            String key = e.getA();
            Provenance prov = e.getB();
            FlatMarshalledProvenance marshalledProvenance = flattenSingleProvenance(prov,key,map);
            outputMap.put(key,marshalledProvenance);
        }

        return new ObjectMarshalledProvenance(name,outputMap,provenance.getClassName(),provenance.getClass().getName());
    }

    private static FlatMarshalledProvenance flattenSingleProvenance(Provenance prov, String key, Map<ObjectProvenance,Integer> map) {
        if (prov instanceof ListProvenance) {
            List<FlatMarshalledProvenance> list = new ArrayList<>();

            for (Provenance p : (ListProvenance<?>)prov) {
                list.add(flattenSingleProvenance(p,key,map));
            }

            return new ListMarshalledProvenance(list);
        } else if (prov instanceof MapProvenance) {
            Map<String,FlatMarshalledProvenance> propMap = new HashMap<>();

            for (Pair<String,? extends Provenance> pair : (MapProvenance<?>)prov) {
                propMap.put(pair.getA(),flattenSingleProvenance(pair.getB(),pair.getA(),map));
            }

            return new MapMarshalledProvenance(propMap);
        } else if (prov instanceof ObjectProvenance) {
            ObjectProvenance objProv = (ObjectProvenance) prov;
            return new SimpleMarshalledProvenance(key, computeName(objProv, map.get(objProv)), objProv);
        } else if (prov instanceof HashProvenance) {
            return new SimpleMarshalledProvenance((HashProvenance) prov);
        } else if (prov instanceof EnumProvenance) {
            return new SimpleMarshalledProvenance((EnumProvenance<?>)prov);
        } else if (prov instanceof PrimitiveProvenance) {
            return new SimpleMarshalledProvenance((PrimitiveProvenance<?>)prov);
        } else {
            throw new ProvenanceException("Unexpected Provenance subclass - found " + prov.getClass().getName() + " expected {ListProvenance, MapProvenance, PrimitiveProvenance, ObjectProvenance}");
        }
    }

    private static void extractProvenanceToQueue(Queue<ObjectProvenance> processingQueue, ObjectProvenance curProv) {
        for (Pair<String, Provenance> p : curProv) {
            Provenance prov = p.getB();
            if (prov instanceof ObjectProvenance) {
                processingQueue.add((ObjectProvenance)prov);
            } else if (prov instanceof ListProvenance) {
                for (Provenance listElement : ((ListProvenance<?>) prov)) {
                    if (listElement instanceof ObjectProvenance) {
                        processingQueue.add((ObjectProvenance)listElement);
                    }
                }
            } else if (prov instanceof MapProvenance) {
                for (Pair<String,? extends Provenance> mapElement : (MapProvenance<?>) prov) {
                    if (mapElement.getB() instanceof ObjectProvenance) {
                        processingQueue.add((ObjectProvenance)mapElement.getB());
                    }
                }
            }
        }
    }

    /**
     * Assumes a closed world (so the names the {@link MarshalledProvenance} objects don't collide).
     * The first element of the list must be the root of the DAG, and it must only have a single root.
     *
     * This method throws {@link ProvenanceException} when the <code>marshalledProvenance</code> list
     * contains malformed objects, the classes are missing, or the appropriate constructors are not available.
     * @param marshalledProvenance The marshalled provenances to unmarshall.
     * @return A provenance.
     */
    public static ObjectProvenance unmarshalProvenance(List<ObjectMarshalledProvenance> marshalledProvenance) {
        Map<String,ObjectProvenance> unmarshalledObjects = new HashMap<>();
        Map<String,ObjectMarshalledProvenance> marshalledObjects = new HashMap<>();

        for (ObjectMarshalledProvenance o : marshalledProvenance) {
            marshalledObjects.put(o.getName(),o);
        }

        return unmarshalProvenance(marshalledProvenance.get(0), unmarshalledObjects, marshalledObjects);
    }

    /**
     * Recursively unmarshalls a single ObjectMarshalledProvenance, updating the two maps to keep track of the
     * state.
     * @param curProv
     * @param unmarshalledObjects
     * @param marshalledObjects
     * @return
     * @throws ProvenanceException
     */
    private static ObjectProvenance unmarshalProvenance(ObjectMarshalledProvenance curProv, Map<String,ObjectProvenance> unmarshalledObjects, Map<String,ObjectMarshalledProvenance> marshalledObjects) throws ProvenanceException {
        String provenanceClassName = curProv.getProvenanceClassName();
        try {
            Class<?> provenanceClass = Class.forName(provenanceClassName);

            if (!ObjectProvenance.class.isAssignableFrom(provenanceClass)) {
                throw new ProvenanceException("ObjectMarshalledProvenance " + curProv + " does not represent a class which implements ObjectProvenance, found " + provenanceClass.getName());
            }
            Map<String, Provenance> arguments = new HashMap<>();

            for (Map.Entry<String, FlatMarshalledProvenance> e : curProv.getMap().entrySet()) {
                Provenance extractedProv = unmarshalFlat(curProv.getName(),e.getValue(),unmarshalledObjects,marshalledObjects);
                arguments.put(e.getKey(),extractedProv);
            }

            Constructor<?> provenanceConstructor = provenanceClass.getConstructor(Map.class);
            ObjectProvenance provenance = (ObjectProvenance) provenanceConstructor.newInstance(arguments);
            return provenance;
        } catch (InstantiationException e) {
            throw new ProvenanceException("Failed to instantiate " + provenanceClassName,e);
        } catch (InvocationTargetException e) {
            throw new ProvenanceException("Exception thrown by " + provenanceClassName + " constructor",e);
        } catch (NoSuchMethodException e) {
            throw new ProvenanceException("No constructor ObjectProvenance(Map<String,Provenance>) found in " + provenanceClassName,e);
        } catch (IllegalAccessException e) {
            throw new ProvenanceException("The ObjectProvenance subclass " + provenanceClassName + " doesn't contain a public constructor which accepts a Map",e);
        } catch (ClassNotFoundException e) {
            throw new ProvenanceException("Failed to find a class called " + provenanceClassName);
        }
    }

    private static Provenance unmarshalFlat(String hostProvName, FlatMarshalledProvenance fmp, Map<String,ObjectProvenance> unmarshalledObjects, Map<String,ObjectMarshalledProvenance> marshalledObjects) {
        if (fmp instanceof SimpleMarshalledProvenance) {
            SimpleMarshalledProvenance smp = (SimpleMarshalledProvenance) fmp;
            if (smp.isReference()) {
                String refName = smp.getValue();
                if (unmarshalledObjects.containsKey(refName)) {
                    return unmarshalledObjects.get(refName);
                } else if (marshalledObjects.containsKey(refName)) {
                    // Need to recurse into the object as it's not been unmarshalled.
                    // First remove it from the list (so if we reference it again it will throw ProvenanceException).
                    ObjectMarshalledProvenance omp = marshalledObjects.remove(refName);
                    // Recurse into the marshalled object provenance
                    ObjectProvenance unmarshalled = unmarshalProvenance(omp, unmarshalledObjects, marshalledObjects);
                    // Put the unmarshalled object provenance into the map and return it.
                    unmarshalledObjects.put(refName, unmarshalled);
                    return unmarshalled;
                } else {
                    throw new ProvenanceException("Invalid provenance object " + hostProvName + " refers to an object called " + refName + " which is not present (or forms a cycle).");
                }
            } else {
                return smp.unmarshallPrimitive();
            }
        } else if (fmp instanceof ListMarshalledProvenance) {
            ListMarshalledProvenance lmp = (ListMarshalledProvenance) fmp;
            List<Provenance> convertedList = new ArrayList<>();
            for (FlatMarshalledProvenance smp : lmp) {
                convertedList.add(unmarshalFlat(hostProvName,smp,unmarshalledObjects,marshalledObjects));
            }
            return new ListProvenance<>(convertedList);
        } else if (fmp instanceof MapMarshalledProvenance) {
            MapMarshalledProvenance mmp = (MapMarshalledProvenance) fmp;
            Map<String,Provenance> convertedMap = new HashMap<>();
            for (Pair<String,FlatMarshalledProvenance> tuple : mmp) {
                convertedMap.put(tuple.getA(), unmarshalFlat(hostProvName,tuple.getB(),unmarshalledObjects,marshalledObjects));
            }
            return new MapProvenance<>(convertedMap);
        } else {
            throw new ProvenanceException("Unexpected FlatMarshalledProvenance subclass, found " + fmp.getClass().getName());
        }
    }
}
