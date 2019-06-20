package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.util.IOUtil;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class ProvenanceUtil {

    private static final Logger logger = Logger.getLogger(ProvenanceUtil.class.getName());

    public enum HashType {
        SHA1("SHA1"), SHA256("SHA-256"), MD5("MD5");

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
            for (Pair<String,Provenance> p : curProv) {
                Provenance prov = p.getB();
                if (prov instanceof ObjectProvenance) {
                    processingQueue.add((ObjectProvenance)prov);
                }
            }
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

                for (Provenance p : (ListProvenance<Provenance>)prov) {
                   if (p instanceof ConfiguredObjectProvenance) {
                        list.add(new SimpleProperty(computeName((ConfiguredObjectProvenance)p,map.get(p))));
                    } else {
                        list.add(new SimpleProperty(p.toString()));
                    }
                }

                data.add(e.getKey(),new ListProperty(list));
            } else if (prov instanceof MapProvenance) {
                Map<String,SimpleProperty> propMap = new HashMap<>();

                for (Pair<String,Provenance> pair : (MapProvenance<Provenance>)prov) {
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

    public static String computeName(ConfiguredObjectProvenance obj, int number) {
        String className = obj.getClassName();
        int lastDot = className.lastIndexOf(".");
        if (lastDot != -1) {
            className = className.substring(lastDot+1);
        }
        return className.toLowerCase() + "-" + number;
    }
}
