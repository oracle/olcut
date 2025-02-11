/*
 * Copyright (c) 2019, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.provenance.impl.NullConfiguredProvenance;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static utilities and helpers for working with Provenance objects.
 */
public final class ProvenanceUtil {

    private static final Logger logger = Logger.getLogger(ProvenanceUtil.class.getName());

    /**
     * The hash types supported for hashing resources.
     */
    public enum HashType {
        SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512"), MD5("MD5");

        public final String name;
        HashType(String name) {
            this.name = name;
        }

        /**
         * Returns a new instance of the appropriate MessageDigest implementation.
         * @return The message digest implementation for this hash type.
         */
        public MessageDigest getDigest() {
            try {
                return MessageDigest.getInstance(name);
            } catch (NoSuchAlgorithmException e) {
                throw new ProvenanceException("Unable to construct MessageDigest for HashType " + name,e);
            }
        }
    }

    private ProvenanceUtil(){}

    /**
     * Used to convert byte arrays into hex strings.
     */
    private final static char[] hexCharacterArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Converts a byte array into a hexadecimal encoded String.
     * <p>
     * Used to convert message digests into Strings.
     * <p>
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

    /**
     * Hashes a list of strings by extracting the bytes using a UTF-8 charset
     * and passing them into the appropriate {@link MessageDigest}.
     * @param hashType The type of hash to perform.
     * @param list The list of strings to hash.
     * @return A hexadecimal string representation of the hash.
     */
    public static String hashList(HashType hashType, List<String> list) {
        MessageDigest md = hashType.getDigest();
        for (String curString : list) {
            md.update(curString.getBytes(StandardCharsets.UTF_8));
        }
        return bytesToHexString(md.digest());
    }

    /**
     * Hashes a file on disk by reading the bytes and passing them through the
     * appropriate {@link MessageDigest}.
     * @param hashType The type of hash to perform.
     * @param path The file.
     * @return A hexadecimal string representation of the hash.
     */
    public static String hashResource(HashType hashType, Path path) {
        return hashResource(hashType,path.toFile());
    }

    /**
     * Hashes a file on disk by reading the bytes and passing them through the
     * appropriate {@link MessageDigest}.
     * @param hashType The type of hash to perform.
     * @param file The file.
     * @return A hexadecimal string representation of the hash.
     */
    public static String hashResource(HashType hashType, File file) {
        MessageDigest md = hashType.getDigest();
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
    }

    /**
     * Hashes a resource stream by reading the bytes and passing them through the
     * appropriate {@link MessageDigest}.
     * <p>
     * If the URL is remote then it logs an error and returns the hash of the URL itself.
     * @param hashType The type of hash to perform.
     * @param file The URL for the stream.
     * @return A hexadecimal string representation of the hash.
     */
    public static String hashResource(HashType hashType, URL file) {
        MessageDigest md = hashType.getDigest();
        if (IOUtil.isDisallowedProtocol(file)) {
            logger.severe("Tried to read disallowed URL protocol: '" + file.toString() + "'");
            return bytesToHexString(md.digest(file.toString().getBytes(StandardCharsets.UTF_8)));
        }
        byte[] buffer = new byte[16384];
        int count;
        try (InputStream bis = new BufferedInputStream(file.openStream())) {
            while ((count = bis.read(buffer)) > 0) {
                md.update(buffer, 0, count);
            }
            return bytesToHexString(md.digest());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException when reading from file " + file);
            return bytesToHexString(md.digest());
        }
    }

    /**
     * Hashes a byte array using the specified {@link HashType}.
     * @param hashType The type of hash to perform.
     * @param input The input array.
     * @return A hexadecimal string representation of the hash.
     */
    public static String hashArray(HashType hashType, byte[] input) {
        MessageDigest md = hashType.getDigest();
        md.update(input);
        return bytesToHexString(md.digest());
    }

    /**
     * Unwraps a {@link ListProvenance} of {@link PrimitiveProvenance}s into a list
     * of the values those provenances hold.
     * @param listProvenance The list to unwrap.
     * @param <T> The type of the primitive provenance value.
     * @param <U> The type of the primitive provenance instance.
     * @return A list of the stored values.
     */
    public static <T, U extends PrimitiveProvenance<T>> List<T> unwrap(ListProvenance<U> listProvenance) {
        List<T> output = new ArrayList<>();

        for (PrimitiveProvenance<T> p : listProvenance) {
            output.add(p.value());
        }

        return output;
    }

    /**
     * Unwraps a {@link MapProvenance} of {@link PrimitiveProvenance}s into a map from string
     * to the values those provenances hold.
     * @param mapProvenance The map to unwrap.
     * @param <T> The type of the primitive provenance value.
     * @param <U> The type of the primitive provenance instance.
     * @return A map of the stored values.
     */
    public static <T, U extends PrimitiveProvenance<T>> Map<String,T> unwrap(MapProvenance<U> mapProvenance) {
        Map<String,T> output = new HashMap<>();

        for (Map.Entry<String,U> p : mapProvenance.getMap().entrySet()) {
            output.put(p.getKey(),p.getValue().value());
        }

        return output;
    }

    /**
     * If the url is a file or jar file url, extract the file modified time and return it.
     * If the file modified time of a jar entry is not available then it tries to
     * get the creation time.
     * <p>
     * Otherwise return {@link Optional#empty}.
     * @param url The url to check
     * @return The {@link Optional#of} {@link OffsetDateTime} or {@link Optional#empty}.
     */
    public static Optional<OffsetDateTime> getModifiedTime(URL url) {
        String protocol = url.getProtocol();
        try {
            if (protocol.equals("file")) {
                File f = new File(url.toURI());
                long modifiedTime = f.lastModified();
                if (modifiedTime != 0L) {
                    OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(modifiedTime), ZoneId.systemDefault());
                    return Optional.of(time);
                }
            } else if (protocol.equals("jar")) {
                URLConnection con = url.openConnection();
                if (con instanceof JarURLConnection jarCon) {
                    JarEntry entry = jarCon.getJarEntry();
                    if (entry != null) {
                        FileTime modifiedTime = entry.getLastModifiedTime();
                        if (modifiedTime != null) {
                            OffsetDateTime time = OffsetDateTime.ofInstant(modifiedTime.toInstant(), ZoneId.systemDefault());
                            return Optional.of(time);
                        } else {
                            FileTime creationTime = entry.getCreationTime();
                            if (creationTime != null) {
                                OffsetDateTime time = OffsetDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                                return Optional.of(time);
                            }
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING,"Error parsing supplied url, failed to find modified time for " + url,e);
        } catch (IOException e) {
            logger.log(Level.WARNING,"IOException when connecting to jar url, failed to find modified time for " + url,e);
        }
        return Optional.empty();
    }

    /**
     * Formats a provenance object with tabs indenting each child object.
     * @param prov Tne provenance to format as a String.
     * @return A formatted String view of the provenance.
     */
    public static String formattedProvenanceString(ObjectProvenance prov) {
        return formattedProvenanceString(prov,0);
    }

    /**
     * Formats a provenance object with tabs indenting the child objects.
     * Starts at the supplied depth.
     * @param prov The provenance to format.
     * @param depth The depth.
     * @return A formatted String view of the provenance, tabbed to the right depth.
     */
    private static String formattedProvenanceString(ObjectProvenance prov, int depth) {
        // Get short class name
        String className = prov.getClassName();
        String shortClassName = className.substring(className.lastIndexOf(".") + 1);

        StringBuilder builder = new StringBuilder();
        // Make indent
        for (int i = 0; i < depth; i++) {
            builder.append('\t');
            builder.append('\t');
        }
        String tabs = builder.toString();
        builder.setLength(0);

        //builder.append(tabs);
        builder.append(shortClassName);
        builder.append("(\n");
        for (Pair<String,Provenance> p : prov) {
            builder.append(tabs);
            builder.append('\t');
            builder.append(p.a());
            builder.append(" = ");
            Provenance innerProv = p.b();
            formatProvenance(innerProv,builder,tabs,depth);
            builder.append('\n');
        }
        builder.append(tabs);
        builder.append(')');

        return builder.toString();
    }

    /**
     * Formats a single provenance, writing to the supplied builder.
     * @param innerProv The provenance to format.
     * @param builder The builder to write to.
     * @param tabs The current tab String.
     * @param depth The current object depth.
     */
    private static void formatProvenance(Provenance innerProv, StringBuilder builder, String tabs, int depth) {
        switch (innerProv) {
            case PrimitiveProvenance<?> primitiveProvenance -> builder.append(primitiveProvenance.value());
            case ListProvenance<?> listProv -> {
                if (listProv.getList().isEmpty()) {
                    builder.append("List[]");
                } else {
                    builder.append("List[\n");
                    for (Provenance provElem : listProv) {
                        builder.append(tabs);
                        builder.append("\t\t");
                        formatProvenance(provElem, builder, tabs, depth + 1);
                        builder.append('\n');
                    }
                    builder.append(tabs);
                    builder.append("\t]");
                }
            }
            case MapProvenance<?> mapProv -> {
                if (mapProv.getMap().isEmpty()) {
                    builder.append("Map{}");
                } else {
                    builder.append("Map{\n");
                    for (Pair<String, ? extends Provenance> provElem : mapProv) {
                        builder.append(tabs);
                        builder.append("\t\t");
                        builder.append(provElem.a());
                        builder.append('=');
                        formatProvenance(provElem.b(), builder, tabs, depth + 1);
                        builder.append('\n');
                    }
                    builder.append(tabs);
                    builder.append("\t}");
                }
            }
            case ObjectProvenance pairs -> {
                String innerProvString = formattedProvenanceString(pairs, depth + 1);
                builder.append(innerProvString);
            }
        }
    }

    /**
     * The values in this map are either String, List or Map.
     * The maps are the same type as the output map, and the Lists contain
     * either Strings, Lists or Maps.
     * <p>
     * It's a weakly typed version of the provenance structures.
     * @param prov The provenance to convert.
     * @return A (possibly nested) map representing the provenance.
     */
    public static Map<String, Object> convertToMap(ObjectProvenance prov) {
        Map<String,Object> output = new HashMap<>();

        for (Pair<String,Provenance> p : prov) {
            String key = p.a();
            Provenance innerProv = p.b();
            Object value = innerConvertToMap(innerProv);
            output.put(key,value);
        }

        return Collections.unmodifiableMap(output);
    }

    /**
     * Converts a provenance into an immutable set of Strings, Lists and Maps.
     * The object graph only contains those three types.
     * @param prov The provenance to convert.
     * @return A structure suitable for display or conversion into JSON.
     */
    private static Object innerConvertToMap(Provenance prov) {
        switch (prov) {
            case PrimitiveProvenance<?> primitiveProvenance -> {
                return String.valueOf(primitiveProvenance.value());
            }
            case ListProvenance<?> listProv -> {
                if (listProv.getList().isEmpty()) {
                    return Collections.emptyList();
                } else {
                    List<Object> list = new ArrayList<>();
                    for (Provenance provElem : listProv) {
                        list.add(innerConvertToMap(provElem));
                    }
                    return Collections.unmodifiableList(list);
                }
            }
            case MapProvenance<?> mapProv -> {
                if (mapProv.getMap().isEmpty()) {
                    return Collections.emptyMap();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    for (Pair<String, ? extends Provenance> provElem : mapProv) {
                        String newKey = provElem.a();
                        Object newValue = innerConvertToMap(provElem.b());
                        map.put(newKey, newValue);
                    }
                    return Collections.unmodifiableMap(map);
                }
            }
            case ObjectProvenance pairs -> {
                return convertToMap(pairs);
            }
        }
    }

    /**
     * Extracts a list of ConfigurationData which can be used to reconstruct the objects
     * recorded in this provenance.
     * <p>
     * This method accepts {@link ObjectProvenance} but returns {@link ConfigurationData} objects
     * only for {@link ConfiguredObjectProvenance} objects that are found when traversing the object
     * graph rooted at {@code provenance}. This is because provenance is a mixture of information
     * computed at runtime and configuration information used to build the runnable objects. The
     * configuration data must be supplied to the objects and the computation re-executed to recreate
     * the provenance.
     * <p>
     * The configurations are given machine generated names, and it makes a best effort
     * attempt to flatten cycles without duplicating objects.
     * <p>
     * This method uses computeName to make the names of the returned ConfigurationData objects.  The
     * component name of the object corresponding to the provenance that is passed in can be retrieved
     * with returnValues.get(0).getName()
     * <p>
     * This method calls {@link #orderProvenances(ObjectProvenance)} and passes the results to
     * {@link #extractConfigurationFromOrdering(ProvenanceOrdering)}.
     * @param provenance The provenance to extract configuration from.
     * @return A list of configurations.
     */
    public static List<ConfigurationData> extractConfiguration(ObjectProvenance provenance) {
        ProvenanceOrdering ordering = orderProvenances(provenance);
        return extractConfigurationFromOrdering(ordering);
    }

    /**
     * Extracts the {@link ConfiguredObjectProvenance}s referenced by the supplied {@link ObjectProvenance},
     * setting a traversal order and giving each one a unique number.
     * @param provenance The provenance to extract.
     * @return An ordering of {@link ConfiguredObjectProvenance}s.
     */
    public static ProvenanceOrdering orderProvenances(ObjectProvenance provenance) {
        IdentityHashMap<ConfiguredObjectProvenance,Integer> provenanceTracker = new IdentityHashMap<>(30);
        List<ConfiguredObjectProvenance> traversalOrder = new ArrayList<>();

        int counter = 0;

        // Extract all the ObjectProvenance instances from the object graph rooted at provenance
        Queue<ObjectProvenance> processingQueue = new ArrayDeque<>();
        processingQueue.add(provenance);
        while (!processingQueue.isEmpty()) {
            ObjectProvenance curProv = processingQueue.poll();
            // skip null provenances
            if (!(curProv instanceof NullConfiguredProvenance)) {
                if (curProv instanceof ConfiguredObjectProvenance confProv) {
                    if (!provenanceTracker.containsKey(confProv)) {
                        provenanceTracker.put(confProv, counter);
                        traversalOrder.add(confProv);
                        counter++;
                    }
                }
                extractProvenanceToQueue(processingQueue, curProv);
            }
        }

        return new ProvenanceOrdering(traversalOrder,provenanceTracker);
    }

    /**
     * Extracts a list of ConfigurationData which can be used to reconstruct the objects
     * recorded in this provenance.
     * <p>
     * The configurations are given machine generated names, and it makes a best effort
     * attempt to flatten cycles without duplicating objects.
     * <p>
     * This method uses computeName to make the names of the returned ConfigurationData objects.  The
     * component name of the object corresponding to the provenance that is passed in can be retrieved
     * with returnValues.get(0).getName()
     * @param ordering The {@link ConfiguredObjectProvenance}s to convert into {@link ConfigurationData}.
     * @return A list of configurations.
     */
    public static List<ConfigurationData> extractConfigurationFromOrdering(ProvenanceOrdering ordering) {
        List<ConfigurationData> output = new ArrayList<>();

        for (int i = 0; i < ordering.traversalOrder.size(); i++) {
            ConfiguredObjectProvenance curProv = ordering.traversalOrder.get(i);
            output.add(extractSingleConfiguration(curProv,computeName(curProv,i),ordering.provenanceTracker));
        }

        return output;
    }

    /**
     * Extracts a single {@link ConfigurationData} from a ConfiguredObjectProvenance, flattening out
     * object references by replacing them with their names.
     * @param obj The object to extract configuration from.
     * @param objName The name of the object to use.
     * @param map The Map of other objects and their ids.
     * @return A configuration for the object.
     */
    private static ConfigurationData extractSingleConfiguration(ConfiguredObjectProvenance obj, String objName, Map<ConfiguredObjectProvenance,Integer> map) {
        ConfigurationData data = new ConfigurationData(objName,obj.getClassName());

        for (Map.Entry<String,Provenance> e : obj.getConfiguredParameters().entrySet()) {
            Provenance prov = e.getValue();
            switch (prov) {
                case ListProvenance<?> listProvenance -> {
                    List<SimpleProperty> list = new ArrayList<>();

                    for (Provenance p : listProvenance) {
                        if (p instanceof ConfiguredObjectProvenance confProv) {
                            // skip nulls
                            if (!(p instanceof NullConfiguredProvenance)) {
                                list.add(new SimpleProperty(computeName(confProv, map.get(confProv))));
                            }
                        } else {
                            list.add(new SimpleProperty(p.toString()));
                        }
                    }

                    data.add(e.getKey(), new ListProperty(list));
                }
                case MapProvenance<?> mapProvenance -> {
                    Map<String, SimpleProperty> propMap = new HashMap<>();

                    for (Pair<String, ? extends Provenance> pair : mapProvenance) {
                        Provenance valueProv = pair.b();
                        if (valueProv instanceof ConfiguredObjectProvenance confProv) {
                            // skip nulls
                            if (!(confProv instanceof NullConfiguredProvenance)) {
                                propMap.put(pair.a(), new SimpleProperty(computeName(confProv, map.get(confProv))));
                            }
                        } else {
                            propMap.put(pair.a(), new SimpleProperty(valueProv.toString()));
                        }
                    }

                    data.add(e.getKey(), new MapProperty(propMap));
                }
                case ConfiguredObjectProvenance confProv -> {
                    // Skip nulls;
                    if (!(confProv instanceof NullConfiguredProvenance)) {
                        data.add(e.getKey(), new SimpleProperty(computeName(confProv, map.get(confProv))));
                    }
                }
                default -> data.add(e.getKey(), new SimpleProperty(prov.toString()));
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

    /**
     * Marshals the provenance into a list of flattened objects.
     * <p>
     * Similar to the configuration extraction, but preserves all the information.
     * @param provenance The provenance to marshal.
     * @return A list of marshalled objects.
     */
    public static List<ObjectMarshalledProvenance> marshalProvenance(ObjectProvenance provenance) {
        Map<ObjectProvenance,Integer> provenanceTracker = new LinkedHashMap<>();

        int counter = 0;

        // Extract all the ObjectProvenance instances from the object graph rooted at provenance
        Queue<ObjectProvenance> processingQueue = new ArrayDeque<>();
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

    /**
     * Marshals a single provenance into a single marshalled object, replacing all the ObjectProvenance
     * fields with references to their name-id tuples (generated using {@link ProvenanceUtil#computeName}.
     * @param provenance The provenance to marshal.
     * @param name The name of the provenance.
     * @param map The map of other provenances in this object graph.
     * @return A single marshalled provenance.
     */
    private static ObjectMarshalledProvenance marshalSingleProvenance(ObjectProvenance provenance, String name, Map<ObjectProvenance,Integer> map) {
        Map<String, FlatMarshalledProvenance> outputMap = new HashMap<>();

        for (Pair<String,Provenance> e : provenance) {
            String key = e.a();
            Provenance prov = e.b();
            FlatMarshalledProvenance marshalledProvenance = flattenSingleProvenance(prov,key,map);
            outputMap.put(key,marshalledProvenance);
        }

        return new ObjectMarshalledProvenance(name,outputMap,provenance.getClassName(),provenance.getClass().getName());
    }

    /**
     * Converts a provenance into a subclass of {@link FlatMarshalledProvenance}. If the provenance is
     * an {@link ObjectProvenance} it's converted into a reference and returns a {@link SimpleMarshalledProvenance}.
     * @param prov The provenance to convert.
     * @param key The name to give the provenance.
     * @param map The map of ObjectProvenances in this object graph.
     * @return A single flattened marshalled provenance.
     */
    private static FlatMarshalledProvenance flattenSingleProvenance(Provenance prov, String key, Map<ObjectProvenance,Integer> map) {
        switch (prov) {
            case ListProvenance<?> listProvenance -> {
                List<FlatMarshalledProvenance> list = new ArrayList<>();

                for (Provenance p : listProvenance) {
                    list.add(flattenSingleProvenance(p, key, map));
                }

                return new ListMarshalledProvenance(list);
            }
            case MapProvenance<?> mapProvenance -> {
                Map<String, FlatMarshalledProvenance> propMap = new HashMap<>();

                for (Pair<String, ? extends Provenance> pair : mapProvenance) {
                    propMap.put(pair.a(), flattenSingleProvenance(pair.b(), pair.a(), map));
                }

                return new MapMarshalledProvenance(propMap);
            }
            case ObjectProvenance objProv -> {
                return new SimpleMarshalledProvenance(key, computeName(objProv, map.get(objProv)), objProv);
            }
            case HashProvenance hashProvenance -> {
                return new SimpleMarshalledProvenance(hashProvenance);
            }
            case EnumProvenance<?> enumProvenance -> {
                @SuppressWarnings("unchecked")
                var smp = new SimpleMarshalledProvenance(enumProvenance);
                return smp;
            }
            case PrimitiveProvenance<?> primitiveProvenance -> {
                return new SimpleMarshalledProvenance(primitiveProvenance);
            }
        }
    }

    /**
     * Extracts all the {@link ObjectProvenance} objects from a single provenance object, adding
     * them to the end of the queue.
     * @param processingQueue The queue of provenance objects.
     * @param curProv The current provenance object.
     */
    private static void extractProvenanceToQueue(Queue<ObjectProvenance> processingQueue, ObjectProvenance curProv) {
        for (Pair<String, Provenance> p : curProv) {
            Provenance prov = p.b();
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
                    if (mapElement.b() instanceof ObjectProvenance) {
                        processingQueue.add((ObjectProvenance)mapElement.b());
                    }
                }
            }
        }
    }

    /**
     * Assumes a closed world (so the names in the {@link MarshalledProvenance} objects don't collide).
     * The first element of the list must be the root of the DAG, and it must only have a single root.
     * <p>
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

        return unmarshalProvenance(marshalledProvenance.getFirst(), unmarshalledObjects, marshalledObjects);
    }

    /**
     * Recursively unmarshalls a single ObjectMarshalledProvenance, updating the two maps to keep track of the
     * state.
     * @param curProv The current marshalled provenance.
     * @param unmarshalledObjects The map of unmarshalled objects.
     * @param marshalledObjects The map of marshalled objects.
     * @return A constructed ObjectProvenance.
     * @throws ProvenanceException If the ObjectProvenance could not be constructed, or if it failed to load the class.
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

    /**
     * Converts a FlatMarshalledProvenance into a Provenance, either by recursively calling {@link ProvenanceUtil#unmarshalFlat} on
     * the elements of a list or map, by recursively calling {@link ProvenanceUtil#unmarshalProvenance} on an ObjectMarshalledProvenance
     * or calling {@link SimpleMarshalledProvenance#unmarshallPrimitive()} on a primitive.
     * <p>
     * Throws provenance exception if there is a cycle or an unexpected class was found.
     * @param hostProvName The host provenance name, used for error messages.
     * @param fmp The marshalled provenance to unmarshal.
     * @param unmarshalledObjects The current map of unmarshalled ObjectProvenances.
     * @param marshalledObjects The current map of marshalled provenances.
     * @return A provenance object.
     */
    private static Provenance unmarshalFlat(String hostProvName, FlatMarshalledProvenance fmp, Map<String,ObjectProvenance> unmarshalledObjects, Map<String,ObjectMarshalledProvenance> marshalledObjects) {
        switch (fmp) {
            case SimpleMarshalledProvenance smp -> {
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
            }
            case ListMarshalledProvenance lmp -> {
                List<Provenance> convertedList = new ArrayList<>();
                for (FlatMarshalledProvenance smp : lmp) {
                    convertedList.add(unmarshalFlat(hostProvName, smp, unmarshalledObjects, marshalledObjects));
                }
                return new ListProvenance<>(convertedList);
            }
            case MapMarshalledProvenance mmp -> {
                Map<String, Provenance> convertedMap = new HashMap<>();
                for (Pair<String, FlatMarshalledProvenance> tuple : mmp) {
                    convertedMap.put(tuple.a(), unmarshalFlat(hostProvName, tuple.b(), unmarshalledObjects, marshalledObjects));
                }
                return new MapProvenance<>(convertedMap);
            }
        }
    }
    
    /**
     * This method can be used for custom implementations of the writeObject method
     * used for customized object serialization to facilitate the serialization of
     * member variables whose type is Provenancable. You should exercise extreme
     * caution when using this method as you are wandering into specialized usage of
     * java serialization which is problematic in its own right. Just because your
     * member satisfies the type of the first parameter does not mean it will
     * serialize and deserialize correctly now or in the future. Therefore, you are
     * advised to <b>avoid using this method</b> but if you do use it, then you
     * should extensively unit test code that depends on this method.
     * 
     * @param provenancable The provenancable object to serialize.
     * @param outputStream The output stream to write to.
     * @throws IOException If the stream couldn't be written to.
     */
    public static void writeObject(Provenancable<? extends ConfiguredObjectProvenance> provenancable,  ObjectOutputStream outputStream) throws IOException {
        ObjectProvenance provenance = provenancable.getProvenance();
        outputStream.writeObject(provenance);
    }

    /**
     * This method can be used for custom implementations of the readObject method
     * used for customized object serialization. Please see the javadoc note for
     * {@link #writeObject(Provenancable, ObjectOutputStream)} for why you should
     * take extra care when deliberating whether or not to use this method.
     * 
     * @param inputStream The stream to read from.
     * @return The object reconstructed from it's provenance.
     * @throws ClassNotFoundException If the class isn't available.
     * @throws IOException If the stream couldn't be read.
     */
    public static Provenancable<? extends ConfiguredObjectProvenance> readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
        ConfiguredObjectProvenance provenance = (ConfiguredObjectProvenance) inputStream.readObject();
        List<ConfigurationData> configurationData = ProvenanceUtil.extractConfiguration(provenance);
        String componentName = configurationData.getFirst().name();
        ConfigurationManager cm = new ConfigurationManager();
        cm.addConfiguration(configurationData);
        @SuppressWarnings("unchecked")
        Provenancable<ConfiguredObjectProvenance> provenancable = (Provenancable<ConfiguredObjectProvenance>) cm.lookup(componentName);
        return provenancable;
    }

    /**
     * A named tuple representing the extraction order of the {@link ConfiguredObjectProvenance}s discovered in a single {@link ObjectProvenance}.
     * <p>
     * It'll be a record one day.
     */
    public static final class ProvenanceOrdering {
        /**
         * The traversal order of the provenances.
         */
        public final List<ConfiguredObjectProvenance> traversalOrder;
        /**
         * The mapping function to an index.
         */
        public final Map<ConfiguredObjectProvenance,Integer> provenanceTracker;

        /**
         * Constructs a ProvenanceOrdering tuple.
         * @param traversalOrder The traversal order of the configured object provenances.
         * @param provenanceTracker The id mapping of the provenances.
         */
        ProvenanceOrdering(List<ConfiguredObjectProvenance> traversalOrder, IdentityHashMap<ConfiguredObjectProvenance,Integer> provenanceTracker) {
            this.traversalOrder = Collections.unmodifiableList(traversalOrder);
            this.provenanceTracker = Collections.unmodifiableMap(provenanceTracker);
        }
    }
}
