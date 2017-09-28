package com.sun.labs.util.props;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A class to hold the information for a serialized Object that is defined in a
 * configuration file.
 */
public class SerializedObject {

    private ConfigurationManager configurationManager;

    private String name;

    private String location;

    private Object object;

    public SerializedObject(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
    
    /**
     * Gets the deserialized object that we represent.
     * @return the object
     * @throws PropertyException if the object cannot be deserialized.
     */
    public Object getObject() throws PropertyException {
        if (object == null) {
            String actualLocation = configurationManager.getGlobalProperties().replaceGlobalProperties(name, null, location);
            InputStream serStream = configurationManager.getInputStreamForLocation(actualLocation);
            if (serStream != null) {
                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024 * 1024))) {
                    //
                    // Read the object and cast it into this class for return;
                    object = ois.readObject();
                } catch (IOException ex) {
                    throw new PropertyException(ex, name, null, "Error reading serialized form from" + actualLocation);
                } catch (ClassNotFoundException ex) {
                    throw new PropertyException(ex, name, null, "Serialized class not found for " + actualLocation);
                }
            }
        }
        return object;
    }

}
