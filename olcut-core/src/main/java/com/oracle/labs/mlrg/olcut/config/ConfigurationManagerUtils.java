package com.oracle.labs.mlrg.olcut.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some static utility methods which ease the handling of system configurations.
 */
public class ConfigurationManagerUtils {

    /**
     * Return the local hostname.
     * @return the local hostname or localhost if it's unknown.
     */
    public static String getHostName()  {
        try {
            return java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /** Shows the current configuration */
    public static void showConfig(ConfigurationManager cm) {
        System.out.println(" ============ config ============= ");
        for(String allName : cm.getInstanceNames(Configurable.class)) {
            showConfig(cm, allName);
        }
    }

    /**
     * Show the configuration for the component with the given name
     *
     * @param name the component name
     */
    public static void showConfig(ConfigurationManager cm, String name) {
        if(!cm.getComponentNames().contains(name)) {
            System.out.println("No component: " + name);
            return;
        }
        System.out.println(name + ":");

        PropertySheet<? extends Configurable> properties = cm.getPropertySheet(name);

        for(String propertyName : properties.getRegisteredProperties()) {
            System.out.print("    " + propertyName + " = ");
            Object obj;
            obj = properties.getRaw(propertyName);
            if(obj instanceof SimpleProperty) {
                System.out.println(obj);
            } else if(obj instanceof List) {
                List l = (List) obj;
                for(Iterator k = l.iterator(); k.hasNext();) {
                    System.out.print(k.next());
                    if(k.hasNext()) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            } else {
                System.out.println("[DEFAULT]");
            }
        }
    }

    /**
     * Imports the system properties into GlobalProperties.
     *
     * @param global global properties
     */
    static void importSystemProperties(GlobalProperties global) {
        Properties props = System.getProperties();
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            String param = (String) e.getKey();
            String value = (String) e.getValue();
            global.setValue(param, value);
        }
    }

    /**
     * Gets a resource associated with the given parameter name given an property sheet.
     *
     * @param name the parameter name
     * @param ps   The property sheet which contains the property
     * @return the resource associated with the name or NULL if it doesn't exist.
     * @throws PropertyException if the resource cannot be found
     */
    public static URL getResource(String name, PropertySheet ps) throws PropertyException {
        URL url;
        Object locationObj = ps.getRaw(name);
        if(locationObj == null) {
            throw new InternalConfigurationException(name, name, "Required resource property '" +
                    name + "' not set");
        }
        String location = locationObj.toString();

        Matcher jarMatcher =
                Pattern.compile("resource:/([.\\w]+?)!(.*)",
                Pattern.CASE_INSENSITIVE).matcher(location);
        if(jarMatcher.matches()) {
            String className = jarMatcher.group(1);
            String resourceName = jarMatcher.group(2);

            try {
                Class cls = Class.forName(className);
                url = cls.getResource(resourceName);
                if(url == null) {
                    // getResource doesn't usually find directories
                    // If the resource is a directory and we
                    // can't find it, we will instead try to  find the class
                    // anchor and backup to the top level and try again
                    String classPath = className.replaceAll("\\.", "/") +
                            ".class";
                    url = cls.getClassLoader().getResource(classPath);
                    if(url != null) {
                        // we should have something like this, so replace everything
                        // jar:file:/foo.jar!/a/b/c/HelloWorld.class
                        // after the ! with the resource name
                        String urlString = url.toString();
                        urlString = urlString.replaceAll("/" + classPath,
                                resourceName);
                        try {
                            url = new URL(urlString);
                        } catch(MalformedURLException mfe) {
                            throw new InternalConfigurationException(mfe, name,
                                    name,
                                    "Bad URL " +
                                    urlString +
                                    mfe.getMessage());
                        }
                    }
                }
                if(url == null) {
                    throw new InternalConfigurationException(name, name, "Can't locate resource " +
                            resourceName);
                } else {
                // System.out.println("URL FOUND " + url);
                }
            } catch(ClassNotFoundException cnfe) {
                throw new InternalConfigurationException(cnfe, name, name, "Can't locate resource:/" +
                        className);
            }
        } else {
            if(!location.contains(":")) {
                location = "file:" + location;
            }

            try {
                url = new URL(location);
            } catch(MalformedURLException e) {
                throw new InternalConfigurationException(e, name, name, "Bad URL " +
                        location +
                        e.getMessage());
            }
        }
        return url;
    }
}
