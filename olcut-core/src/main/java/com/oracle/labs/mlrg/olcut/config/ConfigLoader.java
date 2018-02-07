package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public interface ConfigLoader {

    public static final String CONFIG = "config";
    public static final String COMPONENT = "component";
    public static final String PROPERTY = "property";
    public static final String PROPERTYLIST = "propertylist";
    public static final String PROPERTYMAP = "propertymap";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String SERIALIZED = "serialized";
    public static final String TYPE = "type";
    public static final String ITEM = "item";
    public static final String ENTRY = "entry";
    public static final String FILE = "file";
    public static final String LOCATION = "location";

    public void load(URL url) throws ConfigLoaderException, IOException;

    public String getExtension();

}
