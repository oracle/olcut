package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.io.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperty;
import com.oracle.labs.mlrg.olcut.config.property.ImmutableGlobalProperties;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.xml.XMLConfigFactory;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static com.oracle.labs.mlrg.olcut.config.PropertySheet.StoredFieldType;

/**
 * Manages a set of <code>Configurable</code>s, their parametrization and the relationships between them. Configurations
 * can be specified either by xml or on-the-fly during runtime.
 *
 * @see Configurable
 * @see PropertySheet
 */
public class ConfigurationManager implements Closeable {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());

    public static final Option configFileOption = new Option() {
        public String longName() { return "config-file"; }
        public char charName() { return 'c'; }
        public String usage() { return "A comma separated list of olcut config files."; }
        public Class<? extends Option> annotationType() { return Option.class; }
    };

    public static final Function<String,Option> defaultConfigOptionFunction = (String path) -> new Option() {
        public char charName() { return '\0'; }
        public String longName() { return ""; }
        public String usage() { return "Default configuration is loaded from '" + path + "'."; }
        public Class<? extends Option> annotationType() { return Option.class; }
    };

    public static final Option fileFormatOption = new Option() {
        public String longName() { return "config-file-formats"; }
        public char charName() { return '\0'; }
        public String usage() { return "A comma separated list of olcut FileFormatFactory implementations (assumed to be on the classpath)."; }
        public Class<? extends Option> annotationType() { return Option.class; }
    };

    public static final Option usageOption = new Option() {
        public String longName() { return "usage"; }
        public char charName() { return '\0'; }
        public String usage() { return "Write out this usage/help statement."; }
        public Class<? extends Option> annotationType() { return Option.class; }
    };

    public static final Option helpOption = new Option() {
        public String longName() { return "help"; }
        public char charName() { return '\0'; }
        public String usage() { return "Write out this usage/help statement."; }
        public Class<? extends Option> annotationType() { return Option.class; }
    };

    public static final char ARG_DELIMITER = ',';

    public static final char ESCAPE_CHAR = '\\';

    public static final char WIN_ESCAPE_CHAR = '^';

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static final char CONFIGURABLE_CHAR = '@';

    public static final String SHORT_ARG = "-";

    public static final String LONG_ARG = "--";

    public static final String CONFIGURABLE_OVERRIDE = LONG_ARG + CONFIGURABLE_CHAR;

    // **WARNING** - do not convert this into a Lambda, it doesn't work due to reflection issues.
    public static final Options EMPTY_OPTIONS = new Options(){ };
    //public static final Options EMPTY_OPTIONS = () -> "";

    /**
     * Used to support new config file formats at runtime.
     * Initialised with xml.
     */
    private static final Map<String, FileFormatFactory> formatFactoryMap = Collections.synchronizedMap(new HashMap<>());

    static {
        formatFactoryMap.put("xml",new XMLConfigFactory());
    }

    private List<ConfigurationChangeListener> changeListeners =
            new ArrayList<>();

    protected Map<String, PropertySheet<? extends Configurable>> symbolTable =
            new LinkedHashMap<>();

    protected final Map<ConfigWrapper,PropertySheet<? extends Configurable>> configuredComponents =
            new LinkedHashMap<>();

    protected final Map<String,PropertySheet<? extends Configurable>> addedComponents =
            new LinkedHashMap<>();

    protected final Map<String, ConfigurationData> rawPropertyMap;

    protected final GlobalProperties globalProperties;
    
    protected final Map<String,SerializedObject> serializedObjects;

    protected final GlobalProperties origGlobal;

    protected boolean showCreations;

    private final LinkedList<URL> configURLs = new LinkedList<>();

    private String[] unnamedArguments = new String[0];

    private String usage;

    /**
     * Creates a new empty configuration manager. This constructor is only of use in cases when a system configuration
     * is created during runtime.
     */
    public ConfigurationManager() throws PropertyException, ConfigLoaderException {
        this(new String[0]);
    }

    /**
     * Creates a new configuration manager. Initial properties are loaded from the given location. No need to keep the notion
     * of 'context' around anymore we will just pass around this property manager.
     *
     * @param path place to load initial properties from
     * @throws ConfigLoaderException if an error occurs while loading properties from the location
     */
    public ConfigurationManager(String path) throws PropertyException, ConfigLoaderException {
    	this(new String[]{"-"+configFileOption.charName(),path},EMPTY_OPTIONS);
    }


    /**
     * Creates a new configuration manager. Initial properties are loaded from the given URL. No need to keep the notion
     * of 'context' around anymore we will just pass around this property manager.
     *
     * @param url URL to load initial properties from
     * @throws ConfigLoaderException if an error occurs while loading properties from the URL
     */
    public ConfigurationManager(URL url) throws PropertyException, ConfigLoaderException {
        this(new String[]{"-"+configFileOption.charName(),url.toString()},EMPTY_OPTIONS);
    }

    /**
     * Creates a new configuration manager. Used when all the command line arguments are either: requests for the usage
     * statement, configuration file options, or unnamed.
     * @param arguments An array of command line arguments.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments) throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        this(arguments,EMPTY_OPTIONS);
    }

    /**
     * Creates a new configuration manager. Used when all the command line arguments are either: requests for the usage
     * statement, configuration file options, or unnamed.
     * @param arguments An array of command line arguments.
     * @param defaultConfigPath The default configuration to load.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments, String defaultConfigPath) throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        this(arguments,EMPTY_OPTIONS,defaultConfigPath,true);
    }

    /**
     * Creates a new configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * @param arguments An array of command line arguments.
     * @param options An object to write the parsed argument values into.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments, Options options) throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        this(arguments,options,true);
    }

    /**
     * Creates a new configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * @param arguments An array of command line arguments.
     * @param options An object to write the parsed argument values into.
     * @param useConfigFiles If true, add the config file option. If false ignore the config file option,
     *                       and invalidate any Options that subclass {@link Configurable}.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments, Options options, boolean useConfigFiles)  throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        this(arguments,options,"",useConfigFiles);
    }

    /**
     * Creates a new configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * @param arguments An array of command line arguments.
     * @param options An object to write the parsed argument values into.
     * @param defaultConfigPath The default config path. Set to empty or null to disable.
     * @param useConfigFiles If true, add the config file option. If false ignore the config file option,
     *                       and invalidate any Options that subclass {@link Configurable}.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments, Options options, String defaultConfigPath, boolean useConfigFiles)  throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        // Validate the supplied Options struct is coherent and generate a usage statement.
        usage = validateOptions(options,defaultConfigPath,useConfigFiles);

        // Check if the user requested the usage statement.
        if ((arguments.length == 1) && (arguments[0].equals("--"+usageOption.longName()) || arguments[0].equals("--"+helpOption.longName()))) {
            throw new UsageException(usage);
        }

        // Convert to list so we can remove elements.
        List<String> argumentsList = new LinkedList<>(Arrays.asList(arguments));

        //
        // Parses out configuration files
        List<URL> urls;
        if (useConfigFiles) {
            urls = parseConfigFiles(argumentsList);
            if (urls.isEmpty() && (defaultConfigPath != null) && !defaultConfigPath.isEmpty()) {
                urls.add(findURL(defaultConfigPath,"default-config-file"));
            }
        } else {
            // If we don't have config files then supply an empty list
            urls = new ArrayList<>();
        }

        //
        // Load the configuration files.
        configURLs.addAll(urls);
        URLLoader loader = new URLLoader(configURLs,formatFactoryMap);
        loader.load();
        rawPropertyMap = loader.getPropertyMap();
        globalProperties = loader.getGlobalProperties();
        serializedObjects = new HashMap<>();
        for(Map.Entry<String,SerializedObject> e : loader.getSerializedObjects().entrySet()) {
            e.getValue().setConfigurationManager(this);
            serializedObjects.put(e.getKey(), e.getValue());
        }
        origGlobal = new GlobalProperties(globalProperties);

        //
        // Parses out and sets arguments which override fields in a config file.
        // Writes into the rpd for each component.
        parseConfigurableArguments(argumentsList);

        //
        // Load system properties into global properties
        globalProperties.importSystemProperties();

        //
        // we can't config the configuration manager with itself so we
        // do some of these config items manually.
        GlobalProperty sC = globalProperties.get("showCreations");
        if(sC != null) {
            this.showCreations = "true".equals(sC.getValue());
        }

        //
        // Parses out and sets arguments which are in the supplied options.
        // *Must* be last as it can cause Configurable instantiation.
        // Throws an exception if there are unknown named arguments at this stage.
        try {
            unnamedArguments = parseOptionArguments(argumentsList, options);
        } catch (PropertyException e) {
            throw new ArgumentException(e, e.getMsg() + "\n\n" + usage);
        } catch (IllegalAccessException e) {
            throw new ArgumentException(e, "Failed to write argument into Options");
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new ArgumentException(e, "Failed to instantiate a field of Options.");
        }
    }

    private ConfigurationManager(Map<String, ConfigurationData> newrpm, GlobalProperties newgp, List<ConfigurationChangeListener> newcl, Map<String, PropertySheet<? extends Configurable>> newSymbolTable, Map<String, SerializedObject> newSerializedObjects, GlobalProperties newOrigGlobal) {
        this.rawPropertyMap = newrpm;
        this.globalProperties = newgp;
        this.changeListeners = newcl;
        this.symbolTable = newSymbolTable;
        this.serializedObjects = newSerializedObjects;
        this.origGlobal = newOrigGlobal;
    }

    /**
     * Adds a file format factory to allow ConfigurationManager to read and write that format.
     * @param f The file format factory to add.
     */
    public static void addFileFormatFactory(FileFormatFactory f) {
        formatFactoryMap.put(f.getExtension(),f);
    }

    public static FileFormatFactory getFileFormatFactory(String extension) {
        return formatFactoryMap.get(extension);
    }

    public static String validateOptions(Options options, boolean useConfigFiles) throws ArgumentException {
        return validateOptions(options, "", useConfigFiles);
    }

    public static String validateOptions(Options options, String defaultConfigPath, boolean useConfigFiles) throws ArgumentException {
        Set<Field> optionFields = new HashSet<>();
        Set<Class<? extends Options>> allOptions = Options.getAllOptions(options.getClass());
        StringBuilder builder = new StringBuilder();

        builder.append("Usage:\n\n");
        ArrayList<List<String>> usageList = new ArrayList<>();
        usageList.add(new ArrayList<>(Collections.singletonList("Built-in Options")));
        usageList.add(Options.header);
        if (useConfigFiles) {
            usageList.add(Options.getOptionUsage(configFileOption, "java.lang.String"));
            if (defaultConfigPath != null && !defaultConfigPath.isEmpty()) {
                usageList.add(Options.getOptionUsage(defaultConfigOptionFunction.apply(defaultConfigPath), "java.lang.String"));
            }
            usageList.add(Options.getOptionUsage(fileFormatOption, "java.lang.String"));
        }
        usageList.add(Options.getOptionUsage(usageOption,""));

        for (Class<? extends Options> o : allOptions) {
            usageList.addAll(Options.getUsage(o));
            optionFields.addAll(Options.getOptionFields(o));
        }

        builder.append(Options.formatUsage(usageList));

        //
        // Initialise the option checking with the config file option.
        HashMap<Character,Option> charNameMap = new HashMap<>();
        HashMap<String,Option> longNameMap = new HashMap<>();
        if (useConfigFiles) {
            charNameMap.put(configFileOption.charName(), configFileOption);
            longNameMap.put(configFileOption.longName(), configFileOption);
            longNameMap.put(fileFormatOption.longName(), fileFormatOption);
        }
        longNameMap.put(usageOption.longName(),usageOption);
        longNameMap.put(helpOption.longName(),helpOption);

        for (Field f : optionFields) {
            Option annotation = f.getAnnotation(Option.class);
            FieldType ft = FieldType.getFieldType(f);
            if (ft == null) {
                throw new ArgumentException(annotation.longName(),
                        "Argument has an unsupported type " + f.getType().getName());
            }
            if (!useConfigFiles) {
                if (FieldType.configurableTypes.contains(ft)) {
                    throw new ArgumentException(annotation.longName(),"Argument has a Configurable type, which requires using a config file.");
                } else if (FieldType.listTypes.contains(ft)) {
                    // Now check the generic type of the list.
                    List<Class<?>> list = PropertySheet.getGenericClass(f);
                    if (list.size() == 1) {
                        Class<?> genericClazz = list.get(0);
                        FieldType genericFieldType = FieldType.getFieldType(genericClazz);
                        if (FieldType.configurableTypes.contains(genericFieldType)) {
                            throw new ArgumentException(annotation.longName(),"Argument has a Configurable type, which requires using a config file.");
                        }
                    } else {
                        throw new ArgumentException(annotation.longName(),"Failed to parse the type parameters of the argument.");
                    }
                }
            }
            if (annotation.charName() == '-' || annotation.charName() == Option.SPACE_CHAR) {
                throw new ArgumentException(annotation.longName(),"'-' and ' ' are reserved characters.");
            }
            if (annotation.longName().startsWith("@")) {
                throw new ArgumentException(annotation.longName(),
                        "Arguments starting '--@' are reserved for the configuration system.");
            }
            if ((annotation.charName() != Option.EMPTY_CHAR) && charNameMap.containsKey(annotation.charName())) {
                if ((annotation.charName() == configFileOption.charName()) && useConfigFiles) {
                    throw new ArgumentException("config-file",annotation.longName(),
                            "The -"+configFileOption.charName()+" argument is reserved for the configuration system");
                } else {
                    throw new ArgumentException(charNameMap.get(annotation.charName())
                            .longName(), annotation.longName(), "Two arguments have the same character");
                }
            }
            if (longNameMap.containsKey(annotation.longName())) {
                throw new ArgumentException(longNameMap.get(annotation.longName())
                        .longName(),annotation.longName(),"Two arguments have the same long name");
            }
            charNameMap.put(annotation.charName(),annotation);
            longNameMap.put(annotation.longName(),annotation);
        }

        return builder.toString();
    }

    /**
     * Checks to see if the input after lowercasing is equal to "false" or "true".
     *
     * It's stricter than Boolean.parseBoolean.
     * @param input The input to test
     * @return True if it's a boolean value, false otherwise.
     */
    private static boolean parseableAsBoolean(String input) {
        String lowercase = input.toLowerCase();
        return lowercase.equals("false") || lowercase.equals("true");
    }

    private static URL findURL(String input, String argumentName) {
        URL url = ConfigurationManager.class.getResource(input);
        if (url == null) {
            File file = new File(input);
            if (file.exists()) {
                try {
                    url = file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new ArgumentException(e, argumentName, "Can't load config file: " + input);
                }
            } else {
                try {
                    url = (new URI(input)).toURL();
                } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                    throw new ArgumentException(argumentName, "Can't find config file: " + input);
                }
            }
        }
        return url;
    }

    /**
     * Parses out arguments which override fields in configured objects.
     *
     * Expects arguments of the form --@componentname.propertyname.
     *
     * Throws {@link ArgumentException} if the component name does not match a known component,
     * or if the property name is not valid for that class.
     *
     * Removes the parsed arguments from the input, and overwrites values in the rpd for each component.
     *
     * @param arguments The command line arguments.
     * @throws ArgumentException If an argument is poorly formatted, missing a mandatory parameter, or
     *              does not override a field in a configurable.
     */
    private void parseConfigurableArguments(List<String> arguments) throws ArgumentException {
        Iterator<String> argsItr = arguments.iterator();
        while (argsItr.hasNext()) {
            String curArg = argsItr.next();

            // Check if it's an override for a configurable or global property
            if (curArg.startsWith(CONFIGURABLE_OVERRIDE)) {
                String[] split = curArg.substring(3).split("\\.");
                if (split.length == 2) {
                    ConfigurationData rpd = rawPropertyMap.get(split[0]);
                    if (rpd != null) {
                        if (checkConfigurableField(rpd.getClassName(),split[1])) {
                            // Found a valid configurable field, consume argument.
                            argsItr.remove();
                            if (argsItr.hasNext()) {
                                String param = argsItr.next();
                                List<String> list = parseStringList(param);
                                if (list.size() == 1) {
                                    rpd.add(split[1], new SimpleProperty(list.get(0)));
                                } else {
                                    rpd.add(split[1], ListProperty.createFromStringList(list));
                                }
                                argsItr.remove();
                            } else {
                                throw new ArgumentException(curArg,"No parameter for configurable override argument");
                            }
                        } else {
                            throw new ArgumentException(curArg,"Failed to find field " + split[1] + " in component " + split[0] + " with class " + rpd.getClassName());
                        }
                    } else {
                        throw new ArgumentException(curArg,"Failed to find component " + split[0]);
                    }
                } else if (split.length == 1) {
                    // Override for global property
                    argsItr.remove();
                    if (argsItr.hasNext()) {
                        String param = argsItr.next();
                        globalProperties.setValue(split[0],param);
                        argsItr.remove();
                    } else {
                        throw new ArgumentException(curArg,"No parameter for global property argument");
                    }
                } else {
                    throw new ArgumentException(curArg,"Failed to parse configuration override argument");
                }
            }
        }

    }

    /**
     * Parses out the arguments into the supplied {@link Options}.
     *
     * Removes the parsed arguments from the input.
     *
     * @param arguments The command line arguments.
     * @param options The Options to write the arguments to.
     * @throws ArgumentException If an argument is poorly formatted, missing a mandatory parameter, or not
     *              present in the supplied Options.
     */
    private String[] parseOptionArguments(List<String> arguments, Options options) throws ArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Map<String,Pair<Field,Object>> longNameMap = new HashMap<>();
        Map<Character,Pair<Field,Object>> charNameMap = new HashMap<>();

        arguments = new ArrayList<>(arguments);

        Queue<Options> objectQueue = new LinkedList<>();
        objectQueue.add(options);

        //
        // Populate the argument hashmaps with the field and object to write to.
        while (!objectQueue.isEmpty()) {
            Options o = objectQueue.poll();
            Set<Field> fields = Options.getOptionFields(o.getClass());
            for (Field f : fields) {
                Option ann = f.getAnnotation(Option.class);
                longNameMap.put(ann.longName(),new Pair<>(f,o));
                if (ann.charName() != Option.EMPTY_CHAR) {
                    charNameMap.put(ann.charName(),new Pair<>(f,o));
                }
            }
            fields = Options.getOptions(o.getClass());
            for (Field f : fields) {
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                if (f.get(o) != null) {
                    logger.fine("Warning: overwriting Options field.");
                }
                f.set(o,f.getType().getDeclaredConstructor().newInstance());
                objectQueue.add((Options)f.get(o));
                f.setAccessible(accessible);
            }
        }

        boolean consumed = false;
        for (int i = 0; i < arguments.size(); i++) {
            consumed = false;
            String curArg = arguments.get(i);

            if (curArg.startsWith(LONG_ARG)) {
                String argName = curArg.substring(2);
                Pair<Field,Object> arg = longNameMap.get(argName);
                if (arg != null) {
                    Field f = arg.getA();
                    FieldType ft = FieldType.getFieldType(f);
                    // Consume argument.
                    arguments.remove(i);
                    consumed = true;
                    if (i < arguments.size()) {
                        boolean accessible = f.isAccessible();
                        f.setAccessible(true);
                        String param = arguments.get(i);
                        List<String> list = parseStringList(param);
                        if (FieldType.arrayTypes.contains(ft)) {
                            f.set(arg.getB(), PropertySheet.parseArrayField(this, curArg, f.getName(), f.getType(), ft, ListProperty.createFromStringList(list)));
                        } else if (FieldType.listTypes.contains(ft)) {
                            List<Class<?>> genericList = PropertySheet.getGenericClass(f);
                            if (genericList.size() == 1) {
                                f.set(arg.getB(), PropertySheet.parseListField(this, curArg, f.getName(), f.getType(), genericList.get(0), ft, ListProperty.createFromStringList(list)));
                            } else {
                                f.setAccessible(accessible);
                                throw new ArgumentException(curArg,"Unknown generic type in argument");
                            }
                        } else if (list.size() == 1) {
                            f.set(arg.getB(), PropertySheet.parseSimpleField(this,curArg,f.getName(),f.getType(),ft,list.get(0)));
                        } else {
                            f.setAccessible(accessible);
                            throw new ArgumentException(curArg,"Parsed a list where a single argument was expected. Type = " + f.getType() + ", parsed output = " + list.toString());
                        }
                        // Consume parameter.
                        arguments.remove(i);
                        f.setAccessible(accessible);
                    } else {
                        throw new ArgumentException(curArg,"No parameter for argument");
                    }
                } else {
                    throw new ArgumentException(curArg,"Unknown argument.");
                }
            } else if (curArg.startsWith(SHORT_ARG)) {
                char[] args = curArg.substring(1).toCharArray();
                if (args.length > 0) {
                    arguments.remove(i);
                    consumed = true;
                    //
                    // We'll treat the last argument separately as it might have a parameter
                    for (int j = 0; j < args.length - 1; j++) {
                        Pair<Field,Object> arg = charNameMap.get(args[j]);
                        if (arg != null) {
                            Field f = arg.getA();
                            boolean accessible = f.isAccessible();
                            f.setAccessible(true);
                            FieldType ft = FieldType.getFieldType(f);
                            if (FieldType.isBoolean(ft)) {
                                f.set(arg.getB(),true);
                            } else {
                                f.setAccessible(accessible);
                                throw new ArgumentException(curArg + " on element " + args[j], "Non boolean argument found where boolean expected");
                            }
                            f.setAccessible(accessible);
                        } else {
                            throw new ArgumentException(curArg + " on element " + args[j], "Unknown argument");
                        }
                    }

                    Pair<Field,Object> arg = charNameMap.get(args[args.length-1]);
                    if (arg != null) {
                        Field f = arg.getA();
                        boolean accessible = f.isAccessible();
                        f.setAccessible(true);
                        FieldType ft = FieldType.getFieldType(f);
                        if (FieldType.isBoolean(ft)) {
                            if (i < arguments.size()) {
                                // Check if the next argument is a value coercible to a boolean.
                                // This check is more restrictive than Boolean.parseBoolean which accepts anything as a false value.
                                // It only accepts things which lower case to "false" and "true".
                                String nextArg = arguments.get(i);
                                if (parseableAsBoolean(nextArg)) {
                                    f.set(arg.getB(), Boolean.parseBoolean(nextArg));
                                    arguments.remove(i);
                                } else {
                                    // Next arg is an option or something else, leave unparsed.
                                    f.set(arg.getB(), true);
                                }
                            } else {
                                f.set(arg.getB(), true);
                            }
                        } else {
                            // Now we need to accept the next parameter.
                            curArg = args[args.length-1]+"";
                            if (i < arguments.size()) {
                                String param = arguments.get(i);
                                List<String> list = parseStringList(param);
                                if (FieldType.arrayTypes.contains(ft)) {
                                    f.set(arg.getB(), PropertySheet.parseArrayField(this, curArg, f.getName(), f.getType(), ft, ListProperty.createFromStringList(list)));
                                } else if (FieldType.listTypes.contains(ft)) {
                                    List<Class<?>> genericList = PropertySheet.getGenericClass(f);
                                    if (genericList.size() == 1) {
                                        f.set(arg.getB(), PropertySheet.parseListField(this, curArg, f.getName(), f.getType(), genericList.get(0), ft, ListProperty.createFromStringList(list)));
                                    } else {
                                        f.setAccessible(accessible);
                                        throw new ArgumentException(curArg,"Unknown generic type in argument");
                                    }
                                } else if (list.size() == 1) {
                                    f.set(arg.getB(), PropertySheet.parseSimpleField(this,curArg,f.getName(),f.getType(),ft,list.get(0)));
                                } else {
                                    f.setAccessible(accessible);
                                    throw new ArgumentException(curArg,"Parsed a list where a single argument was expected. Type = " + f.getType() + ", parsed output = " + list.toString());
                                }
                                // Consume parameter.
                                arguments.remove(i);
                            } else {
                                f.setAccessible(accessible);
                                throw new ArgumentException(curArg,"No parameter for argument");
                            }
                        }
                        f.setAccessible(accessible);
                    }
                } else {
                    throw new ArgumentException(curArg, "Empty argument found.");
                }
            }
            if (consumed) {
                i--;
            }
        }

        return arguments.toArray(unnamedArguments);
    }

    /**
     * Parses out the config file argument.
     *
     * Removes the parsed arguments from the input.
     * @param arguments The command line arguments.
     * @return A list of URLs pointing to olcut config files.
     * @throws ArgumentException If an argument is poorly formatted or missing a mandatory parameter.
     */
    private List<URL> parseConfigFiles(List<String> arguments) throws ArgumentException {
        List<URL> urls = new ArrayList<>();

        String confStr = "-" + configFileOption.charName();
        String longStr = "--" + configFileOption.longName();
        String fileFormatStr = "--" + fileFormatOption.longName();
        Iterator<String> argsItr = arguments.iterator();
        while (argsItr.hasNext()) {
            String curArg = argsItr.next();
            if (confStr.equals(curArg) || longStr.equals(curArg)) {
                argsItr.remove();
                if (argsItr.hasNext()) {
                    String curParam = argsItr.next();
                    if (!curParam.startsWith("-")) {
                        List<String> urlList = parseStringList(curParam);
                        urls.clear();
                        for (String s : urlList) {
                            URL url = findURL(s, curArg);
                            urls.add(url);
                        }
                        argsItr.remove();
                    } else {
                        throw new ArgumentException(curArg, "No parameter supplied for argument");
                    }
                } else {
                    throw new ArgumentException(curArg,"No parameter supplied for argument");
                }
            } else if (fileFormatStr.equals(curArg)) {
                argsItr.remove();
                if (argsItr.hasNext()) {
                    String curParam = argsItr.next();
                    if (!curParam.startsWith("-")) {
                        List<String> fileFormatFactoryList = parseStringList(curParam);
                        for (String className : fileFormatFactoryList) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (FileFormatFactory.class.isAssignableFrom(clazz)) {
                                    FileFormatFactory fff = (FileFormatFactory) clazz.getDeclaredConstructor().newInstance();
                                    ConfigurationManager.addFileFormatFactory(fff);
                                } else {
                                    throw new ArgumentException(curArg, className + " does not implement FileFormatFactory");
                                }
                            } catch (ClassNotFoundException e) {
                                throw new ArgumentException(e, curArg, "Class not found");
                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                                throw new ArgumentException(e, curArg, "Could not instantiate class");
                            }
                        }
                        argsItr.remove();
                    } else {
                        throw new ArgumentException(curArg, "No parameter supplied for argument");
                    }
                } else {
                    throw new ArgumentException(curArg,"No parameter supplied for argument");
                }
            }
        }

        return urls;
    }

    public String usage() {
        return usage;
    }

    /**
     * Parses a string into a list of string, respecting escaping of the delimiter, and also allowing quotes.
     * @param input
     * @return
     */
    public static List<String> parseStringList(String input) {
        List<String> tokensList = new ArrayList<>();
        boolean inQuotes = false;
        boolean escaped = false;
        StringBuilder buffer = new StringBuilder();
        if (IS_WINDOWS) {
            for (char c : input.toCharArray()) {
                switch (c) {
                    case ARG_DELIMITER:
                        if (inQuotes || escaped) {
                            buffer.append(c);
                        } else {
                            tokensList.add(buffer.toString());
                            buffer = new StringBuilder();
                        }
                        escaped = false;
                        break;
                    case WIN_ESCAPE_CHAR:
                        escaped = true;
                        break;
                    case '\"':
                        // Note fall through here to gather up the quotes.
                        inQuotes = !inQuotes;
                    default:
                        buffer.append(c);
                        escaped = false;
                        break;
                }
            }
        } else {
            for (char c : input.toCharArray()) {
                switch (c) {
                    case ARG_DELIMITER:
                        if (inQuotes || escaped) {
                            buffer.append(c);
                        } else {
                            tokensList.add(buffer.toString());
                            buffer = new StringBuilder();
                        }
                        escaped = false;
                        break;
                    case ESCAPE_CHAR:
                        escaped = true;
                        break;
                    case '\"':
                        // Note fall through here to gather up the quotes.
                        inQuotes = !inQuotes;
                    default:
                        buffer.append(c);
                        escaped = false;
                        break;
                }
            }
        }
        tokensList.add(buffer.toString());
        return tokensList;
    }

    private boolean checkConfigurableField(String configurableClass, String fieldName) {
        Class<? extends Configurable> clazz = null;
        // catch is empty as this is only called with classes from ConfigurationData,
        // which has already checked it's configurable.
        try {
            clazz = (Class<? extends Configurable>) Class.forName(configurableClass);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load " + configurableClass);
        }
        return checkConfigurableField(clazz,fieldName);
    }

    private boolean checkConfigurableField(Class<? extends Configurable> configurableClass, String fieldName) {
        Set<Field> fields = PropertySheet.getAllFields(configurableClass);
        boolean found = false;
        for (Field f : fields) {
            if (f.getName().equals(fieldName) && (f.getAnnotation(Config.class) != null)) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    logger.warning("Dude seriously, a Map? On the command line? Maps aren't supported.");
                    break;
                } else {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private StoredFieldType getStoredFieldType(String configurableClass, String fieldName) {
        Class<? extends Configurable> clazz = null;
        // catch is empty as this is only called with classes from ConfigurationData,
        // which has already checked it's configurable.
        try {
            clazz = (Class<? extends Configurable>) Class.forName(configurableClass);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load " + configurableClass);
        }
        return getStoredFieldType(clazz,fieldName);
    }

    private StoredFieldType getStoredFieldType(Class<? extends Configurable> configurableClass, String fieldName) {
        Set<Field> fields = PropertySheet.getAllFields(configurableClass);
        for (Field f : fields) {
            if (f.getName().equals(fieldName) && (f.getAnnotation(Config.class) != null)) {
                FieldType ft = FieldType.getFieldType(f);
                if (ft == null) {
                    return StoredFieldType.NONE;
                }
                logger.log(Level.FINEST,"Found field of type " + ft.name());
                //
                // We'll handle things that have list or arrays with items separately.
                if (FieldType.arrayTypes.contains(ft)) {
                    return StoredFieldType.LIST;
                } else if (FieldType.listTypes.contains(ft)) {
                    return StoredFieldType.LIST;
                } else if (FieldType.simpleTypes.contains(ft)) {
                    return StoredFieldType.STRING;
                } else if (FieldType.mapTypes.contains(ft)){
                    return StoredFieldType.MAP;
                } else {
                    return StoredFieldType.NONE;
                }
            }
        }
        return StoredFieldType.NONE;
    }

    /**
     * Adds a set of properties at the given URL to the current configuration
     * manager.
     */
    public void addProperties(URL url) throws ConfigLoaderException {
        configURLs.add(url);

        //
        // We'll make local global properties and raw property data containers
        // so that we can manage the merge ourselves.
        URLLoader loader = new URLLoader(configURLs,formatFactoryMap);
        loader.load();
        GlobalProperties tgp = loader.getGlobalProperties();
        Map<String, ConfigurationData> trpm = loader.getPropertyMap();
        for(Map.Entry<String,SerializedObject> e : loader.getSerializedObjects().entrySet()) {
            e.getValue().setConfigurationManager(this);
            serializedObjects.put(e.getKey(), e.getValue());
        }

        //
        // Now, add the new global properties to the set for this configuration
        // manager, overriding as necessary.  Then do the same thing for the raw
        // property data.
        for(Map.Entry<String, GlobalProperty> e : tgp) {
            globalProperties.setValue(e.getKey(), e.getValue());
            origGlobal.setValue(e.getKey(), e.getValue());
        }
        for(Map.Entry<String, ConfigurationData> e : trpm.entrySet()) {
            ConfigurationData opd = rawPropertyMap.put(e.getKey(), e.getValue());
        }

    }

    /**
     * Overrides a simple property in a specific configurable in this configuration manager.
     *
     * Throws {@link PropertyException} if the configurable/property doesn't exist, has already been instantiated, or isn't a simple property.
     * @param componentName The name of the component.
     * @param propertyName The name of the property/field.
     * @param value The value to set it to.
     */
    public void overrideConfigurableProperty(String componentName, String propertyName, String value) {
        ConfigurationData rpd = rawPropertyMap.get(componentName);
        if (rpd != null) {
            if (!symbolTable.containsKey(componentName)) {
                StoredFieldType type = getStoredFieldType(rpd.getClassName(), propertyName);
                if (type == StoredFieldType.STRING) {
                    rpd.add(propertyName, new SimpleProperty(value));
                } else if (type == StoredFieldType.NONE) {
                    throw new PropertyException(componentName, propertyName, "Failed to find field " + propertyName + " in component " + componentName + " with class " + rpd.getClassName());
                } else {
                    throw new PropertyException(componentName, propertyName, "Incompatible field type, found " + type);
                }
            } else {
                throw new PropertyException(componentName, "Properties can only be overridden before the object is constructed.");
            }
        } else {
            throw new PropertyException(componentName, "Failed to find component " + componentName);
        }
    }

    /**
     * Overrides a list/array/set property in a specific configurable in this configuration manager.
     * <p>
     * Throws {@link PropertyException} if the configurable/property doesn't exist, has already been instantiated, or isn't a list/array/set.
     *
     * @param componentName The name of the component.
     * @param propertyName  The name of the property/field.
     * @param value         The value to set it to.
     */
    public void overrideConfigurableProperty(String componentName, String propertyName, List<String> value) {
        ConfigurationData rpd = rawPropertyMap.get(componentName);
        if (rpd != null) {
            if (!symbolTable.containsKey(componentName)) {
                StoredFieldType type = getStoredFieldType(rpd.getClassName(), propertyName);
                if (type == StoredFieldType.LIST) {
                    rpd.add(propertyName, ListProperty.createFromStringList(value));
                } else if (type == StoredFieldType.NONE) {
                    throw new PropertyException(componentName, propertyName, "Failed to find field " + propertyName + " in component " + componentName + " with class " + rpd.getClassName());
                } else {
                    throw new PropertyException(componentName, propertyName, "Incompatible field type, found " + type);
                }
            } else {
                throw new PropertyException(componentName, "Properties can only be overridden before the object is constructed.");
            }
        } else {
            throw new PropertyException(componentName, "Failed to find component " + componentName);
        }
    }

    /**
     * Overrides a map property in a specific configurable in this configuration manager.
     * <p>
     * Throws {@link PropertyException} if the configurable/property doesn't exist, has already been instantiated, or isn't a map.
     *
     * @param componentName The name of the component.
     * @param propertyName  The name of the property/field.
     * @param value         The value to set it to.
     */
    public void overrideConfigurableProperty(String componentName, String propertyName, Map<String, String> value) {
        ConfigurationData rpd = rawPropertyMap.get(componentName);
        if (rpd != null) {
            if (!symbolTable.containsKey(componentName)) {
                StoredFieldType type = getStoredFieldType(rpd.getClassName(), propertyName);
                if (type == StoredFieldType.MAP) {
                    rpd.add(propertyName, MapProperty.createFromStringMap(value));
                } else if (type == StoredFieldType.NONE) {
                    throw new PropertyException(componentName, propertyName, "Failed to find field " + propertyName + " in component " + componentName + " with class " + rpd.getClassName());
                } else {
                    throw new PropertyException(componentName, propertyName, "Incompatible field type, found " + type);
                }
            } else {
                throw new PropertyException(componentName, "Properties can only be overridden before the object is constructed.");
            }
        } else {
            throw new PropertyException(componentName, "Failed to find component " + componentName);
        }
    }

    /**
     * Shuts down the configuration manager, which is a no-op on the standard version.
     */
    @Override
    public synchronized void close() { }

    /**
     * Get any unnamed arguments that weren't parsed into an {@link Options}
     * instance, or used to override a {@link Configurable} field.
     * @return A string array of command line arguments.
     */
    public String[] getUnnamedArguments() {
        return unnamedArguments;
    }

    /**
     * Gets the raw properties associated with a given instance.
     * @param instanceName the name of the instance whose properties we want
     * @return the associated raw property data, or null if there is no data
     * associated with the given instance name.
     */
    public ConfigurationData getRawProperties(String instanceName) {
        return rawPropertyMap.get(instanceName);
    }

    /**
     * Does this ConfigurationManager know about an instance called instanceName.
     * @param instanceName The name to check.
     * @return True if it contains a {@link Configurable} called instanceName.
     */
    public boolean containsConfigurable(String instanceName) {
        return rawPropertyMap.containsKey(instanceName);
    }

    /**
     * Returns the property sheet for the given object instance
     *
     * @param instanceName the instance name of the object
     * @return the property sheet for the object.
     */
    protected PropertySheet<? extends Configurable> getPropertySheet(String instanceName) {
        if(!symbolTable.containsKey(instanceName)) {
            // if it is not in the symbol table, so construct
            // it based upon our raw property data
            ConfigurationData rpd = rawPropertyMap.get(instanceName);
            if(rpd != null) {
                String className = rpd.getClassName();
                try {
                    Class cls = Class.forName(className);
                    if (Configurable.class.isAssignableFrom(cls)) {

                        // now load the property-sheet by using the class annotation
                        PropertySheet<? extends Configurable> propertySheet =
                                getNewPropertySheet((Class<? extends Configurable>) cls,instanceName, this, rpd);

                        symbolTable.put(instanceName, propertySheet);
                    } else {
                        throw new PropertyException(instanceName, "Unable to cast " + className +
                                " to com.oracle.labs.mlrg.olcut.config.Configurable");
                    }
                } catch(ClassNotFoundException e) {
                    throw new PropertyException(e, "Class " + className + " not found");
                }
            }
        }

        return symbolTable.get(instanceName);
    }

    /**
     * Gets all instances that are of the given type.
     *
     * @param type the desired type of instance
     * @return the set of all instances
     */
    public Collection<String> getInstanceNames(Class<? extends Configurable> type) {
        Collection<String> instanceNames = new ArrayList<>();

        for(PropertySheet ps : symbolTable.values()) {
            if(!ps.isInstantiated()) {
                continue;
            }

            if (type.isAssignableFrom(ps.getClass())) {
                instanceNames.add(ps.getInstanceName());
            }
        }

        return instanceNames;
    }

    /**
     * Returns all names of configurables registered to this instance. The resulting set includes instantiated and
     * uninstantiated components.
     *
     * @return all component named registered to this instance of <code>ConfigurationManager</code>
     */
    public Set<String> getComponentNames() {
        return new HashSet<>(rawPropertyMap.keySet());
    }

    /**
     * Looks up an object that has been specified in the configuration
     * as one that was serialized. Note that such an object does not need
     * to be a component.
     * @param objectName the name of the object to lookup.
     * @return the deserialized object, or <code>null</code> if the object 
     * with that name does not occur in the configuration.
     */
    public Object lookupSerializedObject(String objectName) {
        SerializedObject so = serializedObjects.get(objectName);
        if(so == null) {
            return null;
        }
        return so.getObject();
    }
    /**
     * Looks up a configurable component by its name, instantiating it if
     * necessary.
     *
     * @param instanceName the name of the component that we want.
     * @return the instantiated component, or <code>null</code> if no such named
     * component exists in this configuration.
     * @throws InternalConfigurationException if there is some error instantiating the
     * component.
     */
    public Configurable lookup(String instanceName)
            throws InternalConfigurationException {
        return lookup(instanceName, null, true);
    }

    /**
     * Looks up a configurable component by its name, instantiating it if
     * necessary.
     *
     * @param instanceName the name of the component that we want.
     * @return the instantiated component, or <code>null</code> if no such named
     * component exists in this configuration.
     * @throws InternalConfigurationException if there is some error instantiating the
     * component.
     */
    public Configurable lookup(String instanceName, boolean reuseComponent)
            throws InternalConfigurationException {
        return lookup(instanceName, null, reuseComponent);
    }

    /**
     * Looks up a configurable component by name. If a component registry exists in the 
     * current configuration manager, it will be checked for the given component name.
     * If the component does not exist, it will be created.
     *
     * @param instanceName the name of the component
     * @param cl a listener for this component that is notified when components
     * are added or removed
     * @return the component, or null if a component was not found.
     * @throws InternalConfigurationException If the requested object could not be properly created, or is not a
     *                                        configurable object, or if an error occurred while setting a component
     *                                        property.
     */
    public Configurable lookup(String instanceName, ComponentListener cl)
            throws InternalConfigurationException {
        return lookup(instanceName, null, true);
    }

    /**
     * Looks up a configurable component by name. If a component registry exists
     * in the current configuration manager, it will be checked for the given
     * component name. 
     *
     * @param instanceName the name of the component
     * @param cl a listener for this component that is notified when components
     * are added or removed
     * @param reuseComponent if <code>true</code>, then if the component was 
     * previously created that component will be returned.  If false, then a 
     * new component will be created regardless of whether it had been created
     * before.
     * @return the component, or null if a component was not found.
     * @throws InternalConfigurationException If the requested object could not be properly created, or is not a
     *                                        configurable object, or if an error occurred while setting a component
     *                                        property.
     */
    public Configurable lookup(String instanceName, ComponentListener cl, boolean reuseComponent)
            throws InternalConfigurationException {
        return innerLookup(instanceName,cl,reuseComponent);
    }

    private Configurable innerLookup(String instanceName, ComponentListener cl, boolean reuseComponent)
            throws InternalConfigurationException {
        // apply all new properties to the model
        instanceName = getStrippedComponentName(instanceName);

        //
        // Get the property sheet for this component.
        PropertySheet<? extends Configurable> ps = getPropertySheet(instanceName);

        logger.log(Level.FINER,"lookup: %s", instanceName);
        if(ps == null) {
            throw new PropertyException(instanceName,"Failed to find component.");
        }

        Configurable ret = ps.getOwner(reuseComponent);

        if (ret instanceof Startable) {
            Startable stret = (Startable) ret;
            Thread t = new Thread(stret);
            t.setName(instanceName + "_thread");
            stret.setThread(t);
            t.start();
        }

        //
        // Remember that we configured this component, removing it from the
        // list of added components if necessary.
        configuredComponents.put(new ConfigWrapper(ret), ps);
        addedComponents.remove(instanceName);

        return ret;
    }

    /**
     * Gets the number of added (i.e., uninstantiated components)
     *
     * @return the number of added components in this configuration manager.
     */
    public int getNumAdded() {
        return addedComponents.size();
    }

    /**
     * Gets the number of configured (i.e., instantiated components)
     *
     * @return the number of instantiated components in this configuration manager.
     */
    public int getNumConfigured() {
        return configuredComponents.size();
    }

    /**
     * Looks up a component by class.  Any component defined in the configuration
     * file may be returned.
     *
     * @param c the class that we want
     * @param cl a listener for things of this type
     * @return a component of the given type, or <code>null</code> if there are
     * no components of the given type.
     */

    public <T extends Configurable> T lookup(Class<T> c, ComponentListener<T> cl) {
        List<T> comps = lookupAll(c, cl);
        if(comps.isEmpty()) {
            return null;
        }
        Collections.shuffle(comps);
        return comps.get(0);
    }

    public <T extends Configurable> List<T> lookupAll(Class<T> c) {
        List<T> ret = new ArrayList<>();

        //
        // If the class isn't an interface, then lookup each of the names
        // in the raw property data with the given class
        // name, ignoring those things marked as importable.
        if(!c.isInterface()) {
            String className = c.getName();
            for (Map.Entry<String, ConfigurationData> e : rawPropertyMap.entrySet()) {
                if (e.getValue().getClassName().equals(className) &&
                        !e.getValue().isImportable()) {
                    ret.add((T)lookup(e.getKey()));
                }
            }
        } else {
            //
            // If we have an interface and no registry, lookup all the
            // implementing classes and return them.
            for (Map.Entry<String, ConfigurationData> e : rawPropertyMap.entrySet()) {
                try {
                    Class clazz = Class.forName(e.getValue().getClassName());
                    if (!e.getValue().isImportable() && c.isAssignableFrom(clazz) && !clazz.isInterface()) {
                        ret.add((T)innerLookup(e.getKey(),null,false));
                    }
                } catch (ClassNotFoundException ex) {
                    throw new PropertyException(ex,e.getKey(),"Class not found for component " + e.getKey());
                }
            }
        }

        return ret;
    }

    /**
     * Looks up all components that have a given class name as their type.
     * @param c the class that we want to lookup
     * @param cl a listener that will report when components of the given type
     * are added or removed
     * @return a list of all the components with the given class name as their type.
     */
    public <T extends Configurable> List<T> lookupAll(Class<T> c, ComponentListener<T> cl) {
        return lookupAll(c);
    }

    /**
     * Gets a list of all of the component names of the components that have 
     * a given type.  This will not instantiate the components.
     *
     * @param c the class of the components that we want to look up.
     */
    public <T extends Configurable> List<String> listAll(Class<T> c) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, ConfigurationData> e : rawPropertyMap.entrySet()) {
            ConfigurationData rpd = e.getValue();
            try {
                Class pclass = Class.forName(rpd.getClassName());
                if (c.isAssignableFrom(pclass)) {
                    ret.add(e.getKey());
                }
            } catch(ClassNotFoundException ex) {
                logger.warning(String.format("No class %s found in ConfigurationManager",
                        rpd.getClassName()));
            }
        }

        return ret;
    }

    /**
     * Registers a new configurable to this configuration manager.
     *
     * @param confClass The class of the configurable to be instantiated and to be added to this configuration manager
     *                  instance.
     * @param name      The desired  lookup-name of the configurable
     * @param props     The properties to be used for component configuration
     * @throws IllegalArgumentException if the there's already a component with the same <code>name</code> that's been instantiated by
     *                                  this configuration manager instance.
     */
    public void addConfigurable(Class<? extends Configurable> confClass,
                                String name, Map<String, Property> props) {
        if(name == null) {
            name = confClass.getName();
        }

        if(symbolTable.containsKey(name)) {
            throw new IllegalArgumentException("tried to override existing component name");
        }

        PropertySheet<? extends Configurable> ps = getPropSheetInstanceFromClass(confClass, props, name);
        symbolTable.put(name, ps);
        rawPropertyMap.put(name, new ConfigurationData(name, confClass.getName()));
        addedComponents.put(name, ps);
        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentAdded(this, ps);
        }
    }

    /**
     * Registers a new configurable to this configuration manager.
     *
     * @param confClass    The class of the configurable to be instantiated and to be added to this configuration
     *                     manager instance.
     * @param instanceName The desired  lookup-instanceName of the configurable
     * @throws IllegalArgumentException if the there's already a component with the same <code>instanceName</code>
     *                                  registered to this configuration manager instance.
     */
    public void addConfigurable(Class<? extends Configurable> confClass,
                                String instanceName) {
        addConfigurable(confClass, instanceName, new HashMap<>());
    }

    /**
     * Removes a configurable from this configuration manager.
     * @param name the name of the configurable to remove
     * @return <code>true</code> if a configurable was removed, or <code>false</code> otherwise.
     */
    public boolean removeConfigurable(String name) {

        PropertySheet ps = getPropertySheet(name);
        if(ps == null) {
            return false;
        }

        symbolTable.remove(name);
        rawPropertyMap.remove(name);
        addedComponents.remove(name);

        //
        // If this one's been configured, remove it from there too!
        if(ps.isInstantiated()) {
            configuredComponents.remove(new ConfigWrapper(ps.getOwner()));
        }

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentRemoved(this, ps);
        }
        return true;
    }

    public void addSubConfiguration(ConfigurationManager subCM) {
        addSubConfiguration(subCM, false);
    }

    public void addSubConfiguration(ConfigurationManager subCM, boolean overwrite) {
        Collection<String> compNames = getComponentNames();

        if(!overwrite) {
            for(String addCompName : subCM.getComponentNames()) {
                if(compNames.contains(addCompName)) {
                    throw new RuntimeException(addCompName
                            + " is already registered to system configuration");
                }
            }
            for(String globProp : subCM.globalProperties.keySet()) {
                if(globalProperties.keySet().contains(globProp)) {
                    throw new IllegalArgumentException(globProp
                            + " is already registered as global property");
                }
            }
        }

        globalProperties.putAll(subCM.globalProperties);
        for(PropertySheet ps : subCM.symbolTable.values()) {
            ps.setCM(this);
        }

        symbolTable.putAll(subCM.symbolTable);
        rawPropertyMap.putAll(subCM.rawPropertyMap);
    }

    /**
     * Returns a copy of the map of global properties set for this configuration manager.
     * @return a copy of the global properties.
     */
    public GlobalProperties getGlobalProperties() {
        return new GlobalProperties(globalProperties);
    }

    /**
     * Returns an immutable view of the global properties. Used for efficiency reasons.
     * @return An immutable view on the global properties.
     */
    public ImmutableGlobalProperties getImmutableGlobalProperties() {
        return globalProperties.getImmutableProperties();
    }

    /**
     * Returns a global property.
     *
     * @param propertyName The name of the global property or <code>null</code> if no such property exists
     */
    public String getGlobalProperty(String propertyName) {
        GlobalProperty globProp = globalProperties.get(propertyName);
        if(globProp == null) {
            return null;
        }
        return globalProperties.replaceGlobalProperties("_global", propertyName, globProp.toString());
    }

    /**
     * Returns the url of the xml-configuration which defined this configuration or <code>null</code>  if it was created
     * dynamically.
     */
    public List<URL> getConfigURLs() {
        return configURLs;
    }

    /**
     * Sets a global property.
     *
     * @param propertyName The name of the global property.
     * @param value        The new value of the global property. If the value is <code>null</code> the property becomes
     *                     removed.
     */
    public void setGlobalProperty(String propertyName, String value) {
        if(value == null) {
            globalProperties.remove(propertyName);
            origGlobal.remove(propertyName);
        } else {
            globalProperties.setValue(propertyName, value);
            origGlobal.setValue(propertyName, value);
        }
    }

    public String getStrippedComponentName(String propertyName) {
        assert propertyName != null;

        while(propertyName.startsWith("$")) {
            propertyName = globalProperties.get(GlobalProperty.stripGlobalSymbol(propertyName)).
                    toString();
        }

        return propertyName;
    }

    /** Adds a new listener for configuration change events. */
    public void addConfigurationChangeListener(ConfigurationChangeListener l) {
        if(l == null) {
            return;
        }

        changeListeners.add(l);
    }

    /** Removes a listener for configuration change events. */
    public void removeConfigurationChangeListener(ConfigurationChangeListener l) {
        if(l == null) {
            return;
        }

        changeListeners.remove(l);
    }

    /**
     * Informs all registered <code>ConfigurationChangeListener</code>s about a configuration changes the component
     * named <code>configurableName</code>.
     */
    void fireConfChanged(String configurableName, String propertyName) {
        assert getComponentNames().contains(configurableName);

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.configurationChanged(configurableName, propertyName, this);
        }
    }

    /**
     * Informs all registered <code>ConfigurationChangeListener</code>s about the component previously named
     * <code>oldName</code>
     */
    void fireRenamedConfigurable(String oldName, String newName) {
        assert getComponentNames().contains(newName);

        for(ConfigurationChangeListener changeListener : changeListeners) {
            changeListener.componentRenamed(this, getPropertySheet(newName),
                    oldName);
        }
    }

    /**
     * Test whether the given configuration manager instance equals this instance in terms of same configuration.
     * This equals implementation does not care about instantiation of components.
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof ConfigurationManager)) {
            return false;
        }

        ConfigurationManager cm = (ConfigurationManager) obj;

        Collection<String> setA = new HashSet<>(getComponentNames());
        Collection<String> setB = new HashSet<>(cm.getComponentNames());
        if(!setA.equals(setB)) {
            return false;
        }

        // make sure that all components are the same
        for(String instanceName : getComponentNames()) {
            PropertySheet myPropSheet = getPropertySheet(instanceName);
            PropertySheet otherPropSheet = cm.getPropertySheet(instanceName);

            if(!otherPropSheet.equals(myPropSheet)) {
                return false;
            }
        }

        // make sure that both configuration managers have the same set of global properties
        return cm.getImmutableGlobalProperties().equals(getImmutableGlobalProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredComponents, addedComponents, rawPropertyMap, globalProperties, serializedObjects, origGlobal, showCreations);
    }

    /**
     * Instantiates the given <code>targetClass</code> and instruments it using default properties or the properties
     * given by the <code>defaultProps</code>.
     */
    protected PropertySheet<? extends Configurable> getPropSheetInstanceFromClass(Class<? extends Configurable> targetClass,
                                                                                  Map<String, Property> defaultProps,
                                                                                  String componentName) {
        ConfigurationData rpd = new ConfigurationData(componentName, targetClass.getName());

        for(Map.Entry<String,Property> conf : defaultProps.entrySet()) {
            rpd.add(conf.getKey(), conf.getValue());
        }

        return getNewPropertySheet(targetClass, componentName, this, rpd);
    }

    /**
     * Saves the current configuration to the given file.
     *
     * Only writes out instantiated components.
     *
     * @param file Place to save the configuration.
     * @throws IOException If an error occurs while writing to the file.
     */
    public void save(File file) throws IOException {
        save(file, false);
    }

    /**
     * Saves the current configuration to the given file.
     *
     * @param file Place to save the configuration.
     * @param writeAll If <code>true</code> all components will be written,
     * whether they were instantiated or not.  If <code>false</code>
     * then only those components that were instantiated or added programatically
     * will be written.
     * @throws IOException if an error occurs while writing to the file.
     */
    public void save(File file, boolean writeAll) throws IOException {
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        String extension = i > 0 ? filename.substring(i+1).toLowerCase() : "";
        try (FileOutputStream fos = new FileOutputStream(file)) {
            save(fos, extension, writeAll);
        }
    }

    /**
     * Writes the configuration to the given writer.
     *
     * @param writer The writer to write to.
     * @param extension The extension to write out, which selects the ConfigWriter to use.
     * @param writeAll If <code>true</code> all components will be written,
     * whether they were instantiated or not.  If <code>false</code>
     * then only those components that were instantiated or added programatically
     * will be written.
     * @throws IOException if an error occurs while writing to the file.
     */
    public void save(OutputStream writer, String extension, boolean writeAll) throws IOException {
        FileFormatFactory factory = formatFactoryMap.get(extension);
        if (factory == null) {
            throw new IllegalArgumentException("Extension " + extension + " does not have a registered FileFormatFactory.");
        }
        try {
            ConfigWriter configWriter = factory.getWriter(writer);
            write(configWriter,writeAll);
        } catch (ConfigWriterException e) {
            throw new IOException("Error generating " + extension + " file.", e);
        }
    }

    /**
     * Writes out the configuration to the supplied writer. Closes the writer.
     * @param writer The config writer to use.
     * @param writeAll If <code>true</code> all components will be written,
     * whether they were instantiated or not.  If <code>false</code>
     * then only those components that were instantiated or added programatically
     * will be written.
     * @throws ConfigWriterException If an error occurs while writing the configuration.
     */
    protected void write(ConfigWriter writer, boolean writeAll) throws ConfigWriterException {
        writer.writeStartDocument();
        //
        // Write out the global properties.

        Map<String,String> properties = new HashMap<>();
        for (String propName : origGlobal.keySet()) {
            //
            // Changed to lookup in globalProperties as this has
            // any values overridden on the command line.
            String propVal = globalProperties.get(propName).toString();

            Matcher matcher = GlobalProperty.globalSymbolPattern.matcher(propName);
            propName = matcher.matches() ? matcher.group(1) : propName;

            properties.put(propName,propVal);
        }
        writer.writeGlobalProperties(properties);

        if (!serializedObjects.isEmpty()) {
            writer.writeSerializedObjects(serializedObjects);
        }

        writer.writeStartComponents();
        //
        // A copy of the raw property data that we can use to keep track of what's
        // been written.
        Set<String> allNames = new HashSet<>(rawPropertyMap.keySet());
        for (PropertySheet ps : configuredComponents.values()) {
            ps.save(writer);
            allNames.remove(ps.getInstanceName());
        }

        for (PropertySheet ps : addedComponents.values()) {
            ps.save(writer);
            allNames.remove(ps.getInstanceName());
        }

        //
        // If we're supposed to, write the rest of the stuff.
        if (writeAll) {
            for(String instanceName : allNames) {
                PropertySheet ps = getPropertySheet(instanceName);
                ps.save(writer);
            }
        }
        writer.writeEndComponents();

        writer.writeEndDocument();
        writer.close();
    }

    protected <T extends Configurable> PropertySheet<T> getNewPropertySheet(T conf, String name, ConfigurationManager cm, ConfigurationData rpd) {
        return new PropertySheet<>(conf,name,cm,rpd);
    }

    protected <T extends Configurable> PropertySheet<T> getNewPropertySheet(Class<T> conf, String name, ConfigurationManager cm, ConfigurationData rpd) {
        return new PropertySheet<>(conf,name,cm,rpd);
    }

    /**
     * Imports a configurable component by generating the property sheets
     * necessary to configure the provided component and puts them into
     * this configuration manager.  This is useful in situations where you have
     * a configurable component but you don't have the property sheet that
     * generated it (e.g., if it was sent over the network).
     *
     * @param configurable The configurable component to import.
     * @return The imported configurable's name.
     */
    public String importConfigurable(Configurable configurable) throws PropertyException {
        String configName = "";

        try {
            Set<Field> fields = PropertySheet.getAllFields(configurable.getClass());
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                ConfigurableName nameAnnotation = field.getAnnotation(ConfigurableName.class);
                if (nameAnnotation != null) {
                    configName = (String) field.get(configurable);
                    //
                    // break out of loop at the first instance of ConfigurableName.
                    field.setAccessible(accessible);
                    break;
                }
                field.setAccessible(accessible);
            }
        } catch (IllegalAccessException ex) {
            throw new PropertyException(ex, configName, "Failed to read the ConfigurableName field");
        }

        if (configName.equals("")) {
            throw new PropertyException("", "Failed to extract name from @ConfigurableName field");
        } else {
            return importConfigurable(configurable, configName);
        }
    }

    /**
     * Imports a configurable component by generating the property sheets
     * necessary to configure the provided component and puts them into
     * this configuration manager.  This is useful in situations where you have
     * a configurable component but you don't have the property sheet that
     * generated it (e.g., if it was sent over the network).
     *
     * It's best effort, if your object graph is loopy, it will flatten it
     * into a tree by cloning elements.
     *
     * @param configurable the configurable component to import
     * @param name the unique name to use for the component. This name will be
     * used to prefix any embedded components.
     */
    public String importConfigurable(Configurable configurable,
                                     String name) throws PropertyException {
        Map<String, Property> m = new LinkedHashMap<>();

        ConfigWrapper wrapper = new ConfigWrapper(configurable);
        if (configuredComponents.containsKey(wrapper)) {
            return configuredComponents.get(wrapper).getInstanceName();
        } else if (symbolTable.containsKey(name)) {
            //
            // This throws an exception if the object pointers are different and we're trying to reuse the name.
            throw new PropertyException(name, "Tried to override existing component name");
        }

        //
        // The name of the configuration property for an annotated variable in
        // the configurable class that we were given.
        String propertyName = null;
        Class<? extends Configurable> confClass = configurable.getClass();
        try {
            Set<Field> fields = PropertySheet.getAllFields(confClass);
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Config configAnnotation = field.getAnnotation(Config.class);
                if (configAnnotation != null) {
                    propertyName = field.getName();
                    Class<?> fieldClass = field.getType();
                    FieldType ft = FieldType.getFieldType(fieldClass);
                    List<Class<?>> genericList = PropertySheet.getGenericClass(field);
                    Class<?> genericType = Object.class;
                    if (genericList.size() == 1) {
                        genericType = genericList.get(0);
                    } else if (genericList.size() == 2){
                        genericType = genericList.get(1);
                    }

                    logger.log(Level.FINER, "field %s, class=%s, configurable? %s; genericType=%s configurable? %s",
                            new Object[]{field.getName(),
                                    fieldClass.getCanonicalName(),
                                    Configurable.class.isAssignableFrom(fieldClass),
                                    genericType.getCanonicalName(),
                                    Configurable.class.isAssignableFrom(genericType)
                            });

                    if (FieldType.simpleTypes.contains(ft)) {
                        m.put(propertyName, importSimpleField(fieldClass, name, field.getName(), field.get(configurable)));
                    } else if (FieldType.listTypes.contains(ft)) {
                        m.put(propertyName, importCollection(genericType, name, propertyName, (Collection) field.get(configurable)));
                    } else if (FieldType.arrayTypes.contains(ft)) {
                        Class arrayComponentType = fieldClass.getComponentType();
                        if (Configurable.class.isAssignableFrom(arrayComponentType)) {
                            m.put(propertyName, importCollection(Configurable.class, name, propertyName, Arrays.asList((Configurable[]) field.get(configurable))));
                        } else {
                            List<String> stringList = new ArrayList<>();
                            //
                            // Primitive array
                            if (byte.class.isAssignableFrom(arrayComponentType)) {
                                for (byte b : (byte[]) field.get(configurable)) {
                                    stringList.add("" + b);
                                }
                            } else if (char.class.isAssignableFrom(arrayComponentType)) {
                                for (char c : (char[]) field.get(configurable)) {
                                    stringList.add("" + c);
                                }
                            } else if (short.class.isAssignableFrom(arrayComponentType)) {
                                for (short s : (short[]) field.get(configurable)) {
                                    stringList.add("" + s);
                                }
                            } else if (int.class.isAssignableFrom(arrayComponentType)) {
                                for (int i : (int[]) field.get(configurable)) {
                                    stringList.add("" + i);
                                }
                            } else if (long.class.isAssignableFrom(arrayComponentType)) {
                                for (long l : (long[]) field.get(configurable)) {
                                    stringList.add("" + l);
                                }
                            } else if (float.class.isAssignableFrom(arrayComponentType)) {
                                for (float f : (float[]) field.get(configurable)) {
                                    stringList.add("" + f);
                                }
                            } else if (double.class.isAssignableFrom(arrayComponentType)) {
                                for (double d : (double[]) field.get(configurable)) {
                                    stringList.add("" + d);
                                }
                            } else if (String.class.isAssignableFrom(arrayComponentType)) {
                                stringList.addAll(Arrays.asList((String[]) field.get(configurable)));
                            } else {
                                throw new PropertyException(name, "Unsupported array type " + fieldClass.toString());
                            }
                            m.put(propertyName, ListProperty.createFromStringList(stringList));
                        }
                    } else if (FieldType.mapTypes.contains(ft)) {
                        Map fieldMap = (Map) field.get(configurable);
                        HashMap<String, SimpleProperty> newMap = new HashMap<>();
                        for (Object e : fieldMap.entrySet()) {
                            String key = (String) ((Map.Entry) e).getKey();
                            Object value = ((Map.Entry) e).getValue();
                            newMap.put(key, importSimpleField(genericType,name+"-"+field.getName(),key,value));
                        }
                        m.put(propertyName, new MapProperty(newMap));
                    } else {
                        throw new PropertyException(name, "Unknown field type " +
                                fieldClass.toString() + " found when importing " +
                                name + " of class " + configurable.getClass().toString());
                    }
                }
                field.setAccessible(accessible);
            }
            ConfigurationData rpd = new ConfigurationData(name, confClass.getName(), m);

            PropertySheet<? extends Configurable> ps = getNewPropertySheet(configurable, name, this, rpd);
            symbolTable.put(name, ps);
            rawPropertyMap.put(name, rpd);
            configuredComponents.put(new ConfigWrapper(configurable), ps);
            for(ConfigurationChangeListener changeListener : changeListeners) {
                changeListener.componentAdded(this, ps);
            }
            return name;
        } catch (PropertyException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PropertyException(ex, name, propertyName,
                    String.format("Error importing %s for propName %s",
                            name, propertyName));
        }
    }

    private SimpleProperty importSimpleField(Class type, String prefix, String fieldName, Object input) {
        if (Configurable.class.isAssignableFrom(type)) {
            String newName = prefix + "-" + fieldName;
            return new SimpleProperty(importConfigurable((Configurable) input, newName));
        } else if (Random.class.isAssignableFrom(type)) {
            return new SimpleProperty("" + ((Random) input).nextInt());
        } else {
            return new SimpleProperty(input.toString());
        }
    }

    private ListProperty importCollection(Class innerType, String prefix, String fieldName, Collection input) {
        List<SimpleProperty> propList = new ArrayList<>();
        int i = 0;
        for (Object o : input) {
            String newName = prefix + "-" + fieldName;
            SimpleProperty output = importSimpleField(innerType,newName,""+i,o);
            propList.add(output);
            i++;
        }
        return new ListProperty(propList);
    }

    /**
     * A wrapper for a Configurable that tests for equality. Used in the configuredComponents map.
     */
    private static class ConfigWrapper {

        public final Configurable config;

        ConfigWrapper(Configurable config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ConfigWrapper) {
                return config == ((ConfigWrapper)other).config;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return config.hashCode();
        }

    }
}
