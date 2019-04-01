package com.oracle.labs.mlrg.olcut.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        public final boolean mandatory;
        public final String description;
        public final FieldInfoType type;
        public final String genericListClass;
        public final String genericMapKeyClass;
        public final String genericMapValueClass;

        public FieldInfo(String name, String className, boolean mandatory, String description) {
            this.name = name;
            this.className = className;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.NORMAL;
            this.genericListClass = "";
            this.genericMapKeyClass = "";
            this.genericMapValueClass = "";
        }

        public FieldInfo(String name, String className, boolean mandatory, String description, String genericListClass) {
            this.name = name;
            this.className = className;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.LIST;
            this.genericListClass = genericListClass;
            this.genericMapKeyClass = "";
            this.genericMapValueClass = "";
        }

        public FieldInfo(String name, String className, boolean mandatory, String description, String genericKeyClass, String genericValueClass) {
            this.name = name;
            this.className = className;
            this.mandatory = mandatory;
            this.description = description;
            this.type = FieldInfoType.MAP;
            this.genericListClass = "";
            this.genericMapKeyClass = genericKeyClass;
            this.genericMapValueClass = genericValueClass;
        }
    }

    private static String formatDescription(List<List<String>> descriptions) {
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
        @Option(charName='n',longName="class-name",usage="Name of the Configurable class to describe.")
        public String className;
        @Option(charName='o',longName="output-example-configuration",usage="Emit an example configuration in XML.")
        public boolean output;
    }

    public static void main(String[] args) {
        DescribeOptions o = new DescribeOptions();

        ConfigurationManager cm;
        try {
            cm = new ConfigurationManager(args,o,false);
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
                Set<Field> fieldSet = PropertySheet.getAllFields(configurableClass);

                TreeMap<String, FieldInfo> map = new TreeMap<>();
                for (Field f : fieldSet) {
                    //boolean accessible = f.isAccessible();
                    //f.setAccessible(true);
                    Config configAnnotation = f.getAnnotation(Config.class);
                    if (configAnnotation != null) {
                        //
                        // We have a variable annotated with the Config annotation,
                        // let's get a value out of the property sheet and figure
                        // out how to turn it into the right type.
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
                                    FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),configAnnotation.mandatory(),configAnnotation.description(),genericList.get(0).getCanonicalName());
                                    map.put(f.getName(),fi);
                                } else {
                                    logger.warning("This class has an invalid configurable field called " + f.getName() + ", failed to extract the generic type arguments for a list or set, found: " + genericList.toString());
                                }
                            } else if (FieldType.mapTypes.contains(ft)) {
                                //
                                // Pull out the generic types from the map.
                                List<Class<?>> genericList = PropertySheet.getGenericClass(f);
                                if (genericList.size() == 2) {
                                    FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),configAnnotation.mandatory(),configAnnotation.description(),genericList.get(0).getCanonicalName(),genericList.get(1).getCanonicalName());
                                    map.put(f.getName(),fi);
                                } else {
                                    logger.warning("This class has an invalid configurable field called " + f.getName() + ", failed to extract the generic type arguments for a map, found: " + genericList.toString());
                                }
                            } else {
                                // Else write a standard FieldInfo record.
                                FieldInfo fi = new FieldInfo(f.getName(),f.getType().getName(),configAnnotation.mandatory(),configAnnotation.description());
                                map.put(f.getName(),fi);
                            }
                        }
                    }
                    //f.setAccessible(accessible);
                }

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

                System.out.println("Class: " + configurableClass.getCanonicalName() + "\n");
                System.out.println(formatDescription(output));
            } else {
                logger.warning("The supplied class did not implement Configurable, class = " + clazz.getCanonicalName());
            }
        } catch (ClassNotFoundException e) {
            logger.severe("Failed to load class from name = " + o.className);
        }
    }

}
