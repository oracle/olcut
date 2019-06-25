package com.oracle.labs.mlrg.olcut.config.io;

import java.io.InputStream;
import java.net.URL;

/**
 * An interface for configuration loading from on disk formats like xml or json.
 */
public interface ConfigLoader {

    public static final String CONFIG = "config";
    public static final String COMPONENT = "component";
    public static final String PROPERTY = "property";
    public static final String PROPERTYLIST = "propertylist";
    public static final String PROPERTYMAP = "propertymap";
    public static final String NAME = "name";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String SERIALIZED = "serialized";
    public static final String TYPE = "type";
    public static final String ITEM = "item";
    public static final String ENTRY = "entry";
    public static final String FILE = "file";
    public static final String LOCATION = "location";
    public static final String PROPERTIES = "properties";
    public static final String INHERIT = "inherit";
    public static final String EXPORT = "export";
    public static final String IMPORT = "import";
    public static final String ENTRIES = "entries";
    public static final String LEASETIME = "leasetime";
    public static final String GLOBALPROPERTIES = "global-properties";
    public static final String COMPONENTS = "components";
    public static final String SERIALIZEDOBJECTS = "serialized-objects";
    public static final String FILES = "config-files";

    /**
     * Loads configuration from the supplied URL. Will attempt to load it as a
     * classpath resource first.
     * @param url The URL to load from.
     * @throws ConfigLoaderException Thrown if the configuration is malformed or the URL is not resolvable.
     */
    public void load(URL url) throws ConfigLoaderException;

    /**
     * Loads configuration from the stream.
     *
     * Note: does not close the stream.
     * @param stream The stream to load from.
     * @throws ConfigLoaderException Thrown if the configuration is malformed.
     */
    public void load(InputStream stream) throws ConfigLoaderException;

}
