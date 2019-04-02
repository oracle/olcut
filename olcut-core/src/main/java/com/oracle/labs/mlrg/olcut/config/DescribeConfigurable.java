package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.xml.XMLConfigFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes a Configurable class.
 */
public class DescribeConfigurable {
    private static final Logger logger = Logger.getLogger(DescribeConfigurable.class.getName());

    public static final List<String> header = Collections.unmodifiableList(Arrays.asList("Field Name","Type","Mandatory","Description"));

    private static class FieldInfo {
        public enum FieldInfoType {NORMAL, LIST, MAP}
        public final String name;
        public final String className;
        public final Field field;
        public final boolean mandatory;
        public final String description;
        public final FieldInfoType type;
        public final String genericListClass;
        public final String genericMapKeyClass;
        public final String genericMapValueClass;

        public final String classShortName;

        public FieldInfo(String name, String className, Field field, boolean mandatory, String description) {
            this.name = name;
            this.className = className;
            this.field = field;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.NORMAL;
            this.genericListClass = "";
            this.genericMapKeyClass = "";
            this.genericMapValueClass = "";
            int index = className.lastIndexOf(".");
            this.classShortName = index > -1 ? className.substring(index+1) : className;
        }

        public FieldInfo(String name, String className, Field field, boolean mandatory, String description, String genericListClass) {
            this.name = name;
            this.className = className;
            this.field = field;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.LIST;
            this.genericListClass = genericListClass;
            this.genericMapKeyClass = "";
            this.genericMapValueClass = "";
            int index = className.lastIndexOf(".");
            this.classShortName = index > -1 ? className.substring(index+1) : className;
        }

        public FieldInfo(String name, String className, Field field, boolean mandatory, String description, String genericKeyClass, String genericValueClass) {
            this.name = name;
            this.className = className;
            this.field = field;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.MAP;
            this.genericListClass = "";
            this.genericMapKeyClass = genericKeyClass;
            this.genericMapValueClass = genericValueClass;
            int index = className.lastIndexOf(".");
            this.classShortName = index > -1 ? className.substring(index+1) : className;
        }
    }

    private static String generateDefaultValue(FieldInfo fi) {
        FieldType ft = FieldType.getFieldType(fi.field);
        switch (ft) {
            case STRING:
                return "empty-string";
            case BOOLEAN:
                return "false";
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case ATOMIC_INTEGER:
            case ATOMIC_LONG:
                return "0";
            case CHAR:
                return "c";
            case FLOAT:
            case DOUBLE:
                return "0.0";
            case FILE:
            case PATH:
                return "/path/to/a/file";
            case RANDOM:
                return "42";
            case ENUM:
                try {
                    return Class.forName(fi.className).getEnumConstants()[0].toString();
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class not found when generating default value", e);
                }
            case CONFIGURABLE:
                return fi.classShortName + "-instance";
            default:
                return "invalid-field-type";
        }
    }

    public static TreeMap<String,FieldInfo> generateFieldInfo(Class<? extends Configurable> configurableClass) {
        Set<Field> fieldSet = PropertySheet.getAllFields(configurableClass);

        TreeMap<String, FieldInfo> map = new TreeMap<>();
        for (Field f : fieldSet) {
            //boolean accessible = f.isAccessible();
            Config configAnnotation = f.getAnnotation(Config.class);
            if (configAnnotation != null) {
                FieldType ft = FieldType.getFieldType(f);
                if (ft == null) {
                    logger.warning("This class has an invalid configurable field type for field " + f.getName());
                } else {
                    logger.log(Level.FINEST, "Found field of type " + ft.name());

                    //
                    // Pull out the generic type from the list.
                    if (FieldType.listTypes.contains(ft)) {
                        List<Class<?>> genericList = PropertySheet.getGenericClass(f);
                        if (genericList.size() == 1) {
                            FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),f,configAnnotation.mandatory(),configAnnotation.description(),genericList.get(0).getCanonicalName());
                            map.put(f.getName(),fi);
                        } else {
                            logger.warning("This class has an invalid configurable field called " + f.getName() + ", failed to extract the generic type arguments for a list or set, found: " + genericList.toString());
                        }
                    } else if (FieldType.mapTypes.contains(ft)) {
                        //
                        // Pull out the generic types from the map.
                        List<Class<?>> genericList = PropertySheet.getGenericClass(f);
                        if (genericList.size() == 2) {
                            FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),f,configAnnotation.mandatory(),configAnnotation.description(),genericList.get(0).getCanonicalName(),genericList.get(1).getCanonicalName());
                            map.put(f.getName(),fi);
                        } else {
                            logger.warning("This class has an invalid configurable field called " + f.getName() + ", failed to extract the generic type arguments for a map, found: " + genericList.toString());
                        }
                    } else {
                        // Else write a standard FieldInfo record.
                        FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),f,configAnnotation.mandatory(),configAnnotation.description());
                        map.put(f.getName(),fi);
                    }
                }
            }
        }

        return map;
    }

    public static List<List<String>> generateDescription(Map<String,FieldInfo> map) {
        List<List<String>> output = new ArrayList<>();

        output.add(header);

        for (Map.Entry<String, FieldInfo> e : map.entrySet()) {
            ArrayList<String> fieldString = new ArrayList<>();

            FieldInfo fi = e.getValue();

            String type = fi.className;

            switch (fi.type) {
                case NORMAL:
                    break;
                case LIST:
                    type = type + "<" + fi.genericListClass + ">";
                    break;
                case MAP:
                    type = type + "<" + fi.genericMapKeyClass + "," + fi.genericMapValueClass + ">";
                    break;
            }

            fieldString.add(fi.name);
            fieldString.add(type);
            fieldString.add(""+fi.mandatory);
            fieldString.add(fi.description);

            output.add(fieldString);
        }

        return output;
    }

    public static void writeExampleConfig(OutputStream stream, String fileFormat, Class<? extends Configurable> configurableClass, Map<String,FieldInfo> map) {
        FileFormatFactory factory = ConfigurationManager.getFileFormatFactory(fileFormat);
        ConfigWriter configWriter = factory.getWriter(stream);

        // Generate attributes
        Map<String,String> attributes = new HashMap<>();
        attributes.put(ConfigLoader.NAME,"example");
        attributes.put(ConfigLoader.EXPORT,"false");
        attributes.put(ConfigLoader.IMPORT,"false");
        attributes.put(ConfigLoader.TYPE,configurableClass.getCanonicalName());

        // Generate default properties
        Map<String,Object> properties = new HashMap<>();
        for (Map.Entry<String,FieldInfo> e : map.entrySet()) {
            FieldInfo fi = e.getValue();
            switch (fi.type) {
                case NORMAL:
                    properties.put(e.getKey(),generateDefaultValue(fi));
                    break;
                case LIST:
                    properties.put(e.getKey(),Arrays.asList(fi.className + "-instance"));
                    break;
                case MAP:
                    Map<String,String> newMap = new HashMap<>();
                    newMap.put("mapKey",fi.genericMapValueClass+"-instance");
                    properties.put(e.getKey(),newMap);
                    break;
            }
        }

        configWriter.writeStartDocument();
        configWriter.writeStartComponents();
        configWriter.writeComponent(attributes, properties);
        configWriter.writeEndComponents();
        configWriter.writeEndDocument();
        configWriter.close();
    }

    public static String formatDescription(List<List<String>> descriptions) {
        int[] maxWidth = new int[4];

        for (List<String> a : descriptions) {
            if (a.size() == 4) {
                if (maxWidth[0] < a.get(0).length()) {
                    maxWidth[0] = a.get(0).length();
                }
                if (maxWidth[1] < a.get(1).length()) {
                    maxWidth[1] = a.get(1).length();
                }
                if (maxWidth[2] < a.get(2).length()) {
                    maxWidth[2] = a.get(2).length();
                }
            }
        }

        String formatString = "%-"+maxWidth[0]+"s %-"+maxWidth[1]+"s %-"+maxWidth[2]+"s %s\n";
        StringBuilder builder = new StringBuilder();

        for (List<String> a : descriptions) {
            if (a.size() == 4) {
                builder.append(String.format(formatString,a.get(0),a.get(1),a.get(2),a.get(3)));
            }
        }
        return builder.toString();
    }

    public static class DescribeOptions implements Options {
        @Option(charName='e',longName="file-format",usage="File format to write out, must have an instance of FileFormatFactory on the classpath and added in through the options.")
        public String extension = "xml";
        @Option(charName='n',longName="class-name",usage="Name of the Configurable class to describe.")
        public String className;
        @Option(charName='o',longName="output-example-configuration",usage="Emit an example configuration in XML.")
        public boolean output;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        DescribeOptions o = new DescribeOptions();

        ConfigurationManager cm;
        try {
            cm = new ConfigurationManager(args,o);
        } catch (UsageException e) {
            logger.info(e.getMessage());
            return;
        }

        if (o.className == null || o.className.isEmpty()) {
            logger.info("Please supply a class name.");
            logger.info(cm.usage());
        }

        try {
            Class<?> clazz = Class.forName(o.className);
            if (Configurable.class.isAssignableFrom(clazz)) {
                Class<? extends Configurable> configurableClass = (Class<? extends Configurable>) clazz;

                Map<String,FieldInfo> map = generateFieldInfo(configurableClass);

                List<List<String>> output = generateDescription(map);

                System.out.println("Class: " + configurableClass.getCanonicalName() + "\n");
                System.out.println(formatDescription(output));

                if (o.output) {
                    ByteArrayOutputStream writer = new ByteArrayOutputStream();

                    writeExampleConfig(writer,o.extension,configurableClass,map);

                    System.out.println("Example :\n" + writer.toString("UTF-8"));
                }
            } else {
                logger.warning("The supplied class did not implement Configurable, class = " + clazz.getCanonicalName());
            }
        } catch (ClassNotFoundException e) {
            logger.severe("Failed to load class from name = " + o.className);
        }
    }

}
