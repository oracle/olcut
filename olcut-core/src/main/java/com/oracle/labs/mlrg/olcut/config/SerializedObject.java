package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.util.IOUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A class to hold the information for a serialized Object that is defined in a
 * configuration file.
 */
public class SerializedObject<T> {

    private ConfigurationManager configurationManager;

    private String name;

    private String location;

    private String className;

    private T object;

    public SerializedObject(String name, String location, String className) {
        this.name = name;
        this.location = location;
        this.className = className;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getClassName() {
        return className;
    }
    
    /**
     * Gets the deserialized object that we represent.
     * @return the object
     * @throws PropertyException if the object cannot be deserialized.
     */
    public T getObject() throws PropertyException {
        if (object == null) {
            String actualLocation = configurationManager.getImmutableGlobalProperties().replaceGlobalProperties(name, null, location);
            InputStream serStream = IOUtil.getInputStreamForLocation(actualLocation);
            try {
                Class<T> objectClass = (Class<T>) Class.forName(className);
                if (serStream != null) {
                    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024 * 1024))) {
                        //
                        // Read the object and cast it into this class for return;
                        object = objectClass.cast(ois.readObject());
                    } catch (ClassCastException ex) {
                        throw new PropertyException(ex, name, "Failed to cast object to type " + objectClass.getName());
                    } catch (IOException ex) {
                        throw new PropertyException(ex, name, "Error reading serialized form from " + actualLocation);
                    }
                } else {
                    throw new PropertyException(name, "Failed to open stream from location " + actualLocation);
                }
            } catch (ClassNotFoundException ex) {
                throw new PropertyException(ex, name, "Serialized class " + className + " not found for " + actualLocation);
            }
        }
        return object;
    }

}
