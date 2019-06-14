package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.util.IOUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
}
