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

    /**
     * This method will automatically rename all components of <code>subCM</code> for which there is component named the
     * same in the <code>baseCM</code> .
     *
     * Note: This is ie. required when merging two system configurations into one.
     *
     * @return A map which maps all renamed component names to their new names.
     */
    public static Map<String, String> fixDuplicateNames(ConfigurationManager baseCM,
            ConfigurationManager subCM) {
        Map<String, String> renames = new HashMap<String, String>();

        for(String compName : subCM.getComponentNames()) {
            String uniqueName = compName;

            int i = 0;

            while(baseCM.getComponentNames().contains(uniqueName) ||
                    (subCM.getComponentNames().
                    contains(uniqueName) && !uniqueName.equals(compName))) {

                i++;
                uniqueName = compName + i;
            }

            subCM.renameConfigurable(compName, uniqueName);
            renames.put(compName, uniqueName);
        }

        return renames;
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
            if(obj instanceof String) {
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
            String param = e.getKey().toString();
            String value = e.getValue() == null ? "null" : e.getValue().toString();
            global.setValue(param, value);
        }
    }

    /**
     * Renames a given <code>Configurable</code>. The configurable component named <code>oldName</code> is assumed to be
     * registered to the CM. Renaming does not only affect the configurable itself but possibly global property values
     * and properties of other components.
     */
    static void renameComponent(ConfigurationManager cm, String oldName,
            String newName) {
        assert cm != null;
        assert oldName != null && newName != null;
        if(cm.getPropertySheet(oldName) == null) {
            throw new RuntimeException("no configurable (to be renamed) named " +
                    oldName + " is contained in the CM");
        }

        // this iteration is a little hacky. It would be much better to maintain the links to a configurable in a special table
        for(String instanceName : cm.getComponentNames()) {
            PropertySheet<? extends Configurable> propSheet = cm.getPropertySheet(instanceName);

            for(String propName : propSheet.getRegisteredProperties()) {
                if(propSheet.getRaw(propName) == null) {
                    continue;
                }  // if the property was not defined within the xml-file

                Object o = propSheet.getRaw(propName);
                if (o instanceof List) {
                    List compNames = (List) o;
                    for(int i = 0; i < compNames.size(); i++) {
                        Object element = compNames.get(i);
                        if (element instanceof String) {
                            String compName = (String) element;
                            if (compName.equals(oldName)) {
                                compNames.set(i, newName);
                            }
                        }
                    }
                } else if (o instanceof Map) {
                    Map<String,String> compMap = (Map<String,String>) o;
                    for (String e : compMap.keySet()) {
                        if (compMap.get(e).equals(oldName)) {
                            compMap.put(e,newName);
                        }
                    }
                } else {
                    if(propSheet.getRaw(propName).equals(oldName)) {
                        propSheet.setRaw(propName, newName);
                    }
                }
            }
        }

        PropertySheet ps = cm.getPropertySheet(oldName);
        ps.setInstanceName(newName);

        // it might be possible that the component is the value of a global property
        ImmutableGlobalProperties globalProps = cm.getImmutableGlobalProperties();
        for(String propName : globalProps.keySet()) {
            String propVal = globalProps.get(propName).toString();

            if(propVal.equals(oldName)) {
                cm.setGlobalProperty(propName, newName);
            }
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
        String location = ps.getRaw(name).toString();
        if(location == null) {
            throw new InternalConfigurationException(name, name, "Required resource property '" +
                    name + "' not set");
        }

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
