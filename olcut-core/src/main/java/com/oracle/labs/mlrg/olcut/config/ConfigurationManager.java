/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
import com.oracle.labs.mlrg.olcut.util.IOUtil;
import com.oracle.labs.mlrg.olcut.util.Pair;

import javax.management.MBeanServer;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private static final Pattern WHITESPACE = Pattern.compile("\\s");

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

    public static final char UNIX_ESCAPE_CHAR = '\\';

    public static final char WIN_ESCAPE_CHAR = '^';

    @Deprecated
    public static final char ESCAPE_CHAR = UNIX_ESCAPE_CHAR;

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static final char CUR_ESCAPE_CHAR = IS_WINDOWS ? WIN_ESCAPE_CHAR : UNIX_ESCAPE_CHAR;

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

    protected final Map<String, PropertySheet<? extends Configurable>> symbolTable;

    protected final Map<ConfigWrapper,PropertySheet<? extends Configurable>> configuredComponents =
            new LinkedHashMap<>();

    protected final Map<String, ConfigurationData> configurationDataMap;

    protected final IdentityHashMap<Configurable, String> configurationNameMap;

    protected final GlobalProperties globalProperties;
    
    protected final Map<String,SerializedObject> serializedObjects;

    protected final GlobalProperties origGlobal;

    protected final boolean showCreations;

    private final LinkedList<URL> configURLs = new LinkedList<>();

    private String[] unnamedArguments = new String[0];

    private String usage;

    private MBeanServer mbs;

    /**
     * Creates a new empty configuration manager. This constructor is only of use in cases when a system configuration
     * is created during runtime.
     */
    public ConfigurationManager() throws PropertyException, ConfigLoaderException {
        this(new String[0]);
    }

    /**
     * Creates a new configuration manager. Initial configuration are loaded from the given location.
     *
     * @param path place to load initial configuration from
     * @throws ConfigLoaderException if an error occurs while loading configuration from the location
     */
    public ConfigurationManager(String path) throws PropertyException, ConfigLoaderException {
        this(new String[]{"-"+configFileOption.charName(),path},EMPTY_OPTIONS);
    }


    /**
     * Creates a new configuration manager. Initial configurations are loaded from the given URL.
     *
     * @param url URL to load initial configuration from
     * @throws ConfigLoaderException if an error occurs while loading configuration from the URL
     */
    public ConfigurationManager(URL url) throws PropertyException, ConfigLoaderException {
        this(new String[]{"-"+configFileOption.charName(),url.toString()},EMPTY_OPTIONS);
    }

    /**
     * Creates a new configuration manager. Initial properties are loaded from the supplied list of locations.
     *
     * @param configFilePaths The list of config files to load.
     * @throws PropertyException if there is a configuration error.
     * @throws ConfigLoaderException If an error occurs while loading configuration from the URL.
     */
    public ConfigurationManager(List<String> configFilePaths) throws PropertyException, ConfigLoaderException {
        this(createConfigFileList(configFilePaths),EMPTY_OPTIONS);
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
        this(arguments,Collections.emptyList(),options,defaultConfigPath,useConfigFiles);
    }

    /**
     * Creates a new configuration manager.
     *
     * This constructor performs a sequence of operations:
     * - It validates the supplied options struct to make sure it does not have duplicate option names.
     * - Loads any configuration file specified by the {@link ConfigurationManager#configFileOption}.
     * - Loads in the supplied ConfigurationData objects, overwriting things in the files if necessary.
     * - Parses any configuration overrides and applies them to the configuration manager.
     * - Parses out options for the supplied struct and writes them into the struct.
     * @param arguments An array of command line arguments.
     * @param configData A list of {@link ConfigurationData} objects.
     * @param options An object to write the parsed argument values into.
     * @param defaultConfigPath The default config path. Set to empty or null to disable.
     * @param useConfigFiles If true, add the config file option. If false ignore the config file option,
     *                       and invalidate any Options that subclass {@link Configurable}.
     * @throws UsageException Thrown when the user requested the usage string.
     * @throws ArgumentException Thrown when an argument fails to parse.
     * @throws PropertyException Thrown when an invalid property is loaded.
     * @throws ConfigLoaderException Thrown when the configuration file cannot be read.
     */
    public ConfigurationManager(String[] arguments, List<ConfigurationData> configData, Options options, String defaultConfigPath, boolean useConfigFiles)  throws UsageException, ArgumentException, PropertyException, ConfigLoaderException {
        // Validate the supplied Options struct is coherent and generate a usage statement.
        usage = validateOptions(options,defaultConfigPath,useConfigFiles);

        symbolTable = new LinkedHashMap<>();

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
        configurationDataMap = loader.getPropertyMap();
        configurationNameMap = new IdentityHashMap<>();
        globalProperties = loader.getGlobalProperties();
        serializedObjects = new HashMap<>();
        for(Map.Entry<String,SerializedObject> e : loader.getSerializedObjects().entrySet()) {
            e.getValue().setConfigurationManager(this);
            serializedObjects.put(e.getKey(), e.getValue());
        }
        origGlobal = new GlobalProperties(globalProperties);

        for (ConfigurationData cd : configData) {
            String instanceName = cd.getName();
            if (symbolTable.containsKey(instanceName)) {
                logger.fine("Overwriting " + instanceName + " loaded from file.");
            }

            configurationDataMap.put(instanceName, cd);
        }

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
            this.showCreations = Boolean.parseBoolean(sC.getValue());
        } else {
            this.showCreations = false;
        }

        //
        // Parses out and sets arguments which are in the supplied options.
        // *Must* be last as it can cause Configurable instantiation.
        // Throws an exception if there are unknown named arguments at this stage.
        try {
            unnamedArguments = AccessController.doPrivileged((PrivilegedExceptionAction<String[]>) () -> parseOptionArguments(argumentsList, options));
        } catch (PrivilegedActionException e) {
            Exception inner = e.getException();
            if (inner instanceof IllegalAccessException) {
                throw new ArgumentException(e, "Failed to write argument into Options");
            } else if ((inner instanceof InstantiationException) || (inner instanceof NoSuchMethodException) || (inner instanceof InvocationTargetException)) {
                throw new ArgumentException(e, "Failed to instantiate a field of Options.");
            } else {
                throw new ArgumentException(inner, "Unexpected exception thrown when reading arguments - " + inner.getMessage());
            }
        } catch (PropertyException e) {
            throw new ArgumentException(e, e.getMessage() + "\n\n" + usage);
        }
    }

    private ConfigurationManager(Map<String, ConfigurationData> newrpm, GlobalProperties newgp, Map<String, PropertySheet<? extends Configurable>> newSymbolTable, Map<String, SerializedObject> newSerializedObjects, GlobalProperties newOrigGlobal) {
        this.configurationDataMap = newrpm;
        this.configurationNameMap = new IdentityHashMap<>();
        this.globalProperties = newgp;
        this.symbolTable = newSymbolTable;
        this.serializedObjects = newSerializedObjects;
        this.origGlobal = newOrigGlobal;
        GlobalProperty sC = globalProperties.get("showCreations");
        if(sC != null) {
            this.showCreations = Boolean.parseBoolean(sC.getValue());
        } else {
            this.showCreations = false;
        }
    }

    /**
     * Converts a list of strings referencing paths into a comma separated list suitable for parsing
     * by {@link ConfigurationManager#parseStringList}.
     * @param configFiles The paths to combine.
     * @return A two element string array.
     */
    private static String[] createConfigFileList(List<String> configFiles) {
        StringBuilder sb = new StringBuilder();

        for (String s : configFiles) {
            sb.append(s);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1);

        return new String[]{"-"+configFileOption.charName(),sb.toString()};
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

    /**
     * Validates the options, returning the formatted usage String.
     * <p>
     * A valid options implementation forms a tree of Options implementations,
     * where no two nodes are the same class, and no two {@link Option} instances
     * share the same charName or longName. All {@link Option} instances
     * must be of a configurable type (i.e. be present in {@link FieldType}), and
     * if they reference a configuration file, configuration files must be enabled.
     * {@link Option} charNames must not be '-' or ' ', longNames must not start
     * with '-' or '@', and must not include whitespace.
     * <p>
     * If the options are invalid they throw ArgumentException.
     * @param options The options to validate.
     * @param useConfigFiles If true insert the configuration file related options and check for conflicts with those arguments.
     * @return The usage string.
     * @throws ArgumentException If the options are invalid.
     */
    public static String validateOptions(Options options, boolean useConfigFiles) throws ArgumentException {
        return validateOptions(options, "", useConfigFiles);
    }

    /**
     * Validates the options, returning the formatted usage String.
     * <p>
     * A valid options implementation forms a tree of Options implementations,
     * where no two nodes are the same class, and no two {@link Option} instances
     * share the same charName or longName. All {@link Option} instances
     * must be of a configurable type (i.e. be present in {@link FieldType}), and
     * if they reference a configuration file, configuration files must be enabled.
     * {@link Option} charNames must not be '-' or ' ', longNames must not start
     * with '-' or '@', and must not include whitespace.
     * <p>
     * If the options are invalid they throw ArgumentException.
     * @param options The options to validate.
     * @param defaultConfigPath The default configuration path to display in the usage String.
     * @param useConfigFiles If true insert the configuration file related options and check for conflicts with those arguments.
     * @return The usage string.
     * @throws ArgumentException If the options are invalid.
     */
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
            char charName = annotation.charName();
            String longName = annotation.longName();
            if (ft == null) {
                throw new ArgumentException(longName,
                        "Argument has an unsupported type " + f.getType().getName());
            }
            if (!useConfigFiles) {
                if (FieldType.configurableTypes.contains(ft)) {
                    throw new ArgumentException(longName,"Argument has a Configurable type, which requires using a config file.");
                } else if (FieldType.listTypes.contains(ft)) {
                    // Now check the generic type of the list.
                    List<Class<?>> list = PropertySheet.getGenericClass(f);
                    if (list.size() == 1) {
                        Class<?> genericClazz = list.get(0);
                        FieldType genericFieldType = FieldType.getFieldType(genericClazz);
                        if (FieldType.configurableTypes.contains(genericFieldType)) {
                            throw new ArgumentException(longName,"Argument has a Configurable type, which requires using a config file.");
                        }
                    } else {
                        throw new ArgumentException(longName,"Failed to parse the type parameters of the argument.");
                    }
                }
            }
            if (charName == '-' || charName == Option.SPACE_CHAR) {
                throw new ArgumentException(longName,"'-' and ' ' are reserved characters.");
            }
            if (longName.startsWith("@")) {
                throw new ArgumentException(longName, "Arguments starting '--@' are reserved for the configuration system.");
            }
            if (longName.startsWith("-")) {
                throw new ArgumentException(longName, "Arguments must not start with '-'");
            }
            if (WHITESPACE.matcher(longName).matches()) {
                throw new ArgumentException("'"+longName+"'", "Arguments must not contain whitespace.");
            }
            if ((charName != Option.EMPTY_CHAR) && charNameMap.containsKey(charName)) {
                if ((charName == configFileOption.charName()) && useConfigFiles) {
                    throw new ArgumentException("config-file", longName,
                            "The -"+configFileOption.charName()+" argument is reserved for the configuration system");
                } else {
                    throw new ArgumentException(charNameMap.get(charName).longName(),
                            longName, "Two arguments have the same character");
                }
            }
            if (longNameMap.containsKey(longName)) {
                throw new ArgumentException(longNameMap.get(longName).longName(),
                        longName,"Two arguments have the same long name");
            }
            charNameMap.put(charName,annotation);
            longNameMap.put(longName,annotation);
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
        return AccessController.doPrivileged((PrivilegedAction<URL>)
                () -> {
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
                    if (IOUtil.isDisallowedProtocol(url)) {
                        throw new ConfigLoaderException("Unable to load configurations from URLs with protocol: " + url.getProtocol());
                    }
                    return url;
                }
        );
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
                    ConfigurationData rpd = configurationDataMap.get(split[0]);
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

        boolean consumed;
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

        return arguments.toArray(new String[0]);
    }

    /**
     * Parses out the config file argument.
     *
     * Removes the parsed arguments from the input.
     * @param arguments The command line arguments.
     * @return A list of URLs pointing to olcut config files.
     * @throws ArgumentException If an argument is poorly formatted or missing a mandatory parameter.
     */
    private static List<URL> parseConfigFiles(List<String> arguments) throws ArgumentException {
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
     * @param input A delimiter separated string.
     * @return A list of strings.
     */
    public static List<String> parseStringList(String input) {
        List<String> tokensList = new ArrayList<>();
        boolean inQuotes = false;
        boolean escaped = false;
        StringBuilder buffer = new StringBuilder();
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
                    if ((IS_WINDOWS) && (!escaped)) {
                        escaped = true;
                    } else {
                        buffer.append(c);
                        escaped = false;
                    }
                    break;
                case UNIX_ESCAPE_CHAR:
                    if ((!IS_WINDOWS) && (!escaped)) {
                        escaped = true;
                    } else {
                        buffer.append(c);
                        escaped = false;
                    }
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
        tokensList.add(buffer.toString());
        return tokensList;
    }

    private boolean checkConfigurableField(String configurableClass, String fieldName) {
        StoredFieldType sft = getStoredFieldType(configurableClass,fieldName);
        if (sft == StoredFieldType.MAP) {
            logger.warning("Dude seriously, a Map? On the command line? Maps aren't supported.");
        }
        return sft == StoredFieldType.LIST || sft == StoredFieldType.STRING;
    }

    // Warnings suppressed as it throws PropertyException if given an invalid class name.
    // The class name is checked elsewhere in the code so this should never be thrown.
    @SuppressWarnings("unchecked")
    private StoredFieldType getStoredFieldType(String configurableClass, String fieldName) {
        Class<? extends Configurable> clazz = null;
        // catch is empty as this is only called with classes from ConfigurationData,
        // which has already checked it's configurable.
        try {
            Class<?> tmpClazz = Class.forName(configurableClass);
            if (Configurable.class.isAssignableFrom(tmpClazz)) {
                clazz = (Class<? extends Configurable>) tmpClazz;
            } else {
                throw new PropertyException("",fieldName,configurableClass + " does not extends Configurable.");
            }
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
     * Should object instantiations be logged. Turned on via a system property.
     * @return True if object instantiations should be logged.
     */
    public boolean showCreations() {
        return showCreations;
    }

    /**
     * Adds a set of properties at the given URL to the current configuration
     * manager.
     * @param url The URL of the configuration file to load.
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
            ConfigurationData opd = configurationDataMap.put(e.getKey(), e.getValue());
        }

    }

    /**
     * Overrides a simple property in a specific configurable in this configuration manager.
     *
     * Throws {@link PropertyException} if the configurable/property doesn't exist, has already been instantiated, or doesn't match the field type.
     * @param componentName The name of the component.
     * @param propertyName The name of the property/field.
     * @param value The value to set it to.
     */
    public void overrideConfigurableProperty(String componentName, String propertyName, Property value) {
        ConfigurationData rpd = configurationDataMap.get(componentName);
        if (rpd != null) {
            if (!symbolTable.containsKey(componentName)) {
                StoredFieldType type = getStoredFieldType(rpd.getClassName(), propertyName);
                if ((type == StoredFieldType.STRING) && (value instanceof SimpleProperty)) {
                    rpd.add(propertyName, value);
                } else if ((type == StoredFieldType.LIST) && (value instanceof ListProperty)) {
                    rpd.add(propertyName, value);
                } else if ((type == StoredFieldType.MAP) && (value instanceof MapProperty)) {
                    rpd.add(propertyName, value);
                } else if (type == StoredFieldType.NONE) {
                    throw new PropertyException(componentName, propertyName, "Failed to find field " + propertyName + " in component " + componentName + " with class " + rpd.getClassName());
                } else {
                    throw new PropertyException(componentName, propertyName, "Incompatible field type, found " + type + ", expected " + value.getClass().getSimpleName());
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
     * Get a copy of any unnamed arguments that weren't parsed into an {@link Options}
     * instance, or used to override a {@link Configurable} field.
     * @return A string array of command line arguments.
     */
    public String[] getUnnamedArguments() {
        return Arrays.copyOf(unnamedArguments,unnamedArguments.length);
    }

    /**
     * Gets the configuration data associated with a given instance.
     *
     * Allows the modification of configuration for future objects.
     * @param instanceName the name of the instance whose properties we want
     * @return the associated configuration data, or {@link Optional#empty} if there is no data
     * associated with the given instance name.
     */
    public Optional<ConfigurationData> getConfigurationData(String instanceName) {
        ConfigurationData data = configurationDataMap.get(instanceName);
        if (data == null) {
            return Optional.empty();
        } else {
            return Optional.of(data);
        }
    }

    /**
     * Does this ConfigurationManager know about an instance called instanceName.
     *
     * Does not trigger class instantiation or configuration.
     * @param instanceName The name to check.
     * @return True if it contains a {@link Configurable} called instanceName.
     */
    public boolean containsConfigurable(String instanceName) {
        return configurationDataMap.containsKey(instanceName);
    }

    /**
     * If the given argument was instantiated by this ConfigurationManager, returns the name of the instance used
     * to generate it.
     *
     * @param conf the configurable instance whose name we want
     * @return The name from the original config file, or {@link Optional#empty} if conf was not instantiated by
     * this configuration manager.
     */
    public Optional<String> getConfiguredName(Configurable conf) {
        if(configurationNameMap.containsKey(conf)) {
            return Optional.of(configurationNameMap.get(conf));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the property sheet for the given object instance
     *
     * @param instanceName the instance name of the object
     * @return the property sheet for the object.
     */
    @SuppressWarnings("unchecked") // Warning suppressed as it's behind an isAssignableFrom check.
    protected PropertySheet<? extends Configurable> getPropertySheet(String instanceName) {
        if(!symbolTable.containsKey(instanceName)) {
            // if it is not in the symbol table, so construct
            // it based upon our raw property data
            ConfigurationData rpd = configurationDataMap.get(instanceName);
            if(rpd != null) {
                String className = rpd.getClassName();
                try {
                    Class<?> confClass = Class.forName(className);
                    if (Configurable.class.isAssignableFrom(confClass)) {
                        PropertySheet<? extends Configurable> propertySheet = new PropertySheet<>((Class<? extends Configurable>)confClass,this,rpd);
                        symbolTable.put(instanceName, propertySheet);
                    } else {
                        throw new PropertyException(rpd.getName(), "Class " + className + " does not implement Configurable.");
                    }
                } catch (ClassNotFoundException e) {
                    throw new PropertyException(e, rpd.getName(), "Class " + className + " not found");
                }
            }
        }

        return symbolTable.get(instanceName);
    }

    /**
     * Gets all instances that are of the given type.
     *
     * Only returns the names of the instantiated objects.
     *
     * @param type the desired type of instance
     * @return the set of all instances
     */
    public Set<String> getInstanceNames(Class<? extends Configurable> type) {
        Set<String> instanceNames = new HashSet<>();

        for (PropertySheet ps : symbolTable.values()) {
            if (!ps.isInstantiated()) {
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
        return new HashSet<>(configurationDataMap.keySet());
    }

    /**
     * Looks up an object that has been specified in the configuration
     * as one that was serialized. Note that such an object does not need
     * to be configurable.
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
     * necessary. If the component was previously created, the same instance
     * will be returned.
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
     * @param reuseComponent If false creates a fresh instance of the desired component.
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
     * If the component does not exist, it will be created, otherwise the existing
     * instance will be returned.
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
        return lookup(instanceName, cl, true);
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

        logger.log(Level.FINER,String.format("lookup: %s", instanceName));
        if(ps == null) {
            throw new PropertyException(instanceName,"Failed to find component.");
        }

        Configurable ret = ps.getOwner(reuseComponent);

        if(!configurationNameMap.containsKey(ret)) {
            configurationNameMap.put(ret, instanceName);
        }

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

        return ret;
    }

    /**
     * Gets the number of configured (i.e., instantiated components)
     *
     * @return the number of instantiated components in this configuration manager.
     */
    public int getNumInstantiated() {
        return configuredComponents.size();
    }

    /**
     * Looks up a component by class.  Any component defined in the configuration
     * file may be returned.
     *
     * @param c the class that we want
     * @param cl a listener for things of this type
     * @param <T> The type of the Configurable expected.
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

    /**
     * Looks up all the components of a given type, returning a map of them.
     * <p>
     * If the class is an interface, it returns all the configurables which implement that interface,
     * if it's a concrete class then it returns only those configurables which are exactly that class.
     * @param c The class of component to lookup.
     * @param <T> The type of the component.
     * @return A map containing all instances of the desired class this configuration manager knows about.
     */
    @SuppressWarnings("unchecked") // Casts to T are implicitly checked as we use Class<T> to find the names.
    public <T extends Configurable> Map<String, T> lookupAllMap(Class<T> c) {
        Map<String, T> ret = new HashMap<>();

        //
        // If the class isn't an interface, then lookup each of the names
        // in the raw property data with the given class
        // name, ignoring those things marked as importable.
        if(!c.isInterface()) {
            String className = c.getName();
            for (Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
                if (e.getValue().getClassName().equals(className) &&
                        !e.getValue().isImportable()) {
                    ret.put(e.getKey(),(T)lookup(e.getKey()));
                }
            }
        } else {
            //
            // If we have an interface and no registry, lookup all the
            // implementing classes and return them.
            for (Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
                try {
                    Class clazz = Class.forName(e.getValue().getClassName());
                    if (!e.getValue().isImportable() && c.isAssignableFrom(clazz) && !clazz.isInterface()) {
                        ret.put(e.getKey(),(T)innerLookup(e.getKey(),null,true));
                    }
                } catch (ClassNotFoundException ex) {
                    throw new PropertyException(ex,e.getKey(),"Class not found for component " + e.getKey());
                }
            }
        }

        return ret;
    }

    /**
     * Looks up all the components of a given type, returning a list of them.
     * @param c The class of component to lookup.
     * @param <T> The type of the component.
     * @return A list containing all instances of the desired class this configuration manager knows about.
     */
    @SuppressWarnings("unchecked") // Casts to T are implicitly checked as we use Class<T> to find the names.
    public <T extends Configurable> List<T> lookupAll(Class<T> c) {
        List<T> ret = new ArrayList<>();

        //
        // If the class isn't an interface, then lookup each of the names
        // in the raw property data with the given class
        // name, ignoring those things marked as importable.
        if(!c.isInterface()) {
            String className = c.getName();
            for (Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
                if (e.getValue().getClassName().equals(className) &&
                        !e.getValue().isImportable()) {
                    ret.add((T)lookup(e.getKey()));
                }
            }
        } else {
            //
            // If we have an interface and no registry, lookup all the
            // implementing classes and return them.
            for (Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
                try {
                    Class clazz = Class.forName(e.getValue().getClassName());
                    if (!e.getValue().isImportable() && c.isAssignableFrom(clazz) && !clazz.isInterface()) {
                        ret.add((T)innerLookup(e.getKey(),null,true));
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
     * @param <T> The type of the component.
     * @return a list of all the components with the given class name as their type.
     */
    public <T extends Configurable> List<T> lookupAll(Class<T> c, ComponentListener<T> cl) {
        return lookupAll(c);
    }

    /**
     * Looks for a single instance of a specified Configurable in the configuration. If there is
     * no such instance, this method returns null. If there is more than one instance, an
     * exception is thrown. lookupSingleton can optionally include any class that may be
     * assignable (a subclass or implementation) to the provided type. Note that this
     * could potentially allow a user to load their own class. If the security of the
     * class you're looking up is important, declare it final to prevent code insertion.
     *
     * @param c The Class of component to lookup.
     * @param allowAssignable allow types that are assignable to the given class to match
     * @param <T> The type of the component.
     * @return the one instance of the desired class this configuration manager knows about or null if
     *         no such instance is present
     * @throws PropertyException if there is more than one instance
     */
    @SuppressWarnings("unchecked") // Casts to T are implicitly checked as we use Class<T> to find the names.
    public <T extends Configurable> T lookupSingleton(Class<T> c, boolean allowAssignable) throws PropertyException {

        List<String> instanceNames = new ArrayList<>();
        for(Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
            ConfigurationData rpd = e.getValue();
            try {
                Class pclass = Class.forName(rpd.getClassName());
                if (!rpd.isImportable() &&
                        ((allowAssignable && c.isAssignableFrom(pclass)) ||
                         (!allowAssignable && rpd.getClassName().equals(c.getName())))) {
                    instanceNames.add(e.getKey());
                }
            } catch(ClassNotFoundException ex) {
                logger.warning(String.format("No class %s found in ConfigurationManager",
                        rpd.getClassName()));
            }
        }

        //
        // Check that we got only one instance and that it is not an interface.
        if (instanceNames.isEmpty()) {
            return null;
        }
        if (instanceNames.size() > 1) {
            String names = instanceNames.stream().collect(Collectors.joining(", "));
            throw new PropertyException("", "Multiple instances of " + c.getName() + " found in configuration: " + names);
        }

        String matchedName = instanceNames.get(0);
        ConfigurationData cd = configurationDataMap.get(matchedName);
        try {
            Class matchedClass = Class.forName(cd.getClassName());
            if (!matchedClass.isInterface()) {
                return (T)lookup(matchedName);
            } else {
                throw new PropertyException("matchedName", "Cannot instantiate component with type "
                        + matchedClass + " since it is an interface");
            }
        } catch (ClassNotFoundException e) {
            throw new PropertyException(e,matchedName,"Class not found for component " + matchedName);
        }
    }


    /**
     * Gets a list of all of the component names of the components that have 
     * a given type.  This will not instantiate the components.
     *
     * @param c the class of the components that we want to look up.
     * @param <T> The type of the component.
     * @return A list of component names.
     */
    public <T extends Configurable> List<String> listAll(Class<T> c) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, ConfigurationData> e : configurationDataMap.entrySet()) {
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
     * Adds an empty ConfigurationData with the specified name and specified class.
     *
     * @param confClass    The class of the configurable to be added to this configuration
     *                     manager instance.
     * @param instanceName The desired lookup name of the configurable, if it's empty or null it's replaced with {@link Class#getSimpleName()}.
     * @throws IllegalArgumentException if the there's already a configurable with the same <code>instanceName</code>
     *                                  registered to this configuration manager instance.
     */
    public void addConfiguration(Class<? extends Configurable> confClass,
                                 String instanceName) {
        if (instanceName == null || instanceName.isEmpty()) {
            instanceName = confClass.getSimpleName();
        }

        ConfigurationData data = new ConfigurationData(instanceName,confClass.getName());
        addConfiguration(data);
    }

    /**
     * Removes a configurable from this configuration manager.
     * @param name the name of the configurable to remove
     * @return <code>true</code> if a configurable was removed, or <code>false</code> otherwise.
     */
    public boolean removeConfigurable(String name) {
        if (configurationDataMap.containsKey(name)) {
            configurationDataMap.remove(name);

            if (symbolTable.containsKey(name)) {
                PropertySheet<? extends Configurable> ps = symbolTable.remove(name);
                //
                // If this one's been instantiated, remove it from there too!
                if(ps.isInstantiated()) {
                    configuredComponents.remove(new ConfigWrapper(ps.getOwner()));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void addSubConfiguration(ConfigurationManager subCM) {
        addSubConfiguration(subCM, false);
    }

    public void addSubConfiguration(ConfigurationManager subCM, boolean overwrite) {
        Collection<String> compNames = getComponentNames();

        if(!overwrite) {
            for(String addCompName : subCM.getComponentNames()) {
                if(compNames.contains(addCompName)) {
                    throw new PropertyException(addCompName, addCompName
                            + " is already registered to system configuration");
                }
            }
            for(String globProp : subCM.globalProperties.keySet()) {
                if(globalProperties.keySet().contains(globProp)) {
                    throw new PropertyException(globProp, globProp
                            + " is already registered as global property");
                }
            }
        }

        globalProperties.putAll(subCM.globalProperties);
        for(Map.Entry<String,PropertySheet<? extends Configurable>> e : subCM.symbolTable.entrySet()) {
            PropertySheet<? extends Configurable> newPS = e.getValue().copy();
            newPS.setCM(this);
            symbolTable.put(e.getKey(),newPS);
        }

        configurationDataMap.putAll(subCM.configurationDataMap);
    }


    /**
     * Registers a new ConfigurationData with this ConfigurationManager. Does not trigger instantiation of the
     * object or class loading.
     *
     * @param newData     The properties to be used for component configuration
     * @throws IllegalArgumentException if the there's already a component with the same <code>name</code> that's been instantiated by
     *                                  this configuration manager instance.
     */
    public void addConfiguration(ConfigurationData newData) {
        String instanceName = newData.getName();
        if (symbolTable.containsKey(instanceName)) {
            throw new IllegalArgumentException("tried to override existing instantiated component name");
        }

        configurationDataMap.put(instanceName, newData);
    }

    /**
     * Adds all the configurations in the list to this ConfigurationManager.
     *
     * Does not trigger class loading or validation of the ConfigurationData objects.
     * @param newData The configurations to ingest.
     */
    public void addConfiguration(List<ConfigurationData> newData) {
        for (ConfigurationData data : newData) {
            addConfiguration(data);
        }
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
     * @return The value associated with the named global property.
     */
    public String getGlobalProperty(String propertyName) {
        GlobalProperty globProp = globalProperties.get(propertyName);
        if(globProp == null) {
            return null;
        }
        return globalProperties.replaceGlobalProperties("_global", propertyName, globProp.toString());
    }

    /**
     * Returns the urls of the configuration which defined this configuration or an empty list
     * if it was created dynamically.
     * @return The list of URLs this ConfigurationManager contains.
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

    protected String getStrippedComponentName(String propertyName) {
        assert propertyName != null;

        while(propertyName.startsWith("$")) {
            propertyName = globalProperties.get(GlobalProperty.stripGlobalSymbol(propertyName)).
                    toString();
        }

        return propertyName;
    }

    /**
     * Test whether the given configuration manager instance equals this instance in terms of same configuration.
     * This equals implementation does not care about instantiation of components.
     * @param obj The object to compare to.
     * @return true if the object contains the same configurations.
     */
    @Override
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
            ConfigurationData myData = configurationDataMap.get(instanceName);
            ConfigurationData otherData = cm.configurationDataMap.get(instanceName);

            if(!myData.equals(otherData)) {
                return false;
            }
        }

        // make sure that both configuration managers have the same set of global properties
        return cm.getImmutableGlobalProperties().equals(getImmutableGlobalProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredComponents, configurationDataMap, globalProperties, serializedObjects, origGlobal, showCreations);
    }

    /**
     * Saves the current configuration to the given file.
     *
     * Only writes out instantiated components, and redacts their fields if required.
     *
     * @param file Place to save the configuration.
     * @throws IOException If an error occurs while writing to the file.
     */
    public void save(File file) throws IOException {
        save(file, false);
    }

    /**
     * Saves the current configuration to the given file.
     * <p>
     * Instantiated components have their field values redacted, uninstantiated components
     * write out all their data.
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
     * Writes the configuration to the given stream. Does not close the stream.
     * <p>
     * Instantiated components have their field values redacted, uninstantiated components
     * write out all their data.
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
        // The names of all the known configurations, so we can keep track of what we've written.
        Set<String> allNames = new HashSet<>(configurationDataMap.keySet());
        for (PropertySheet<?> ps : configuredComponents.values()) {
            configurationDataMap.get(ps.getInstanceName()).save(writer,ps.getRedactedFieldNames());

            allNames.remove(ps.getInstanceName());
        }

        //
        // If we're supposed to, write the rest of the stuff.
        if (writeAll) {
            for(String instanceName : allNames) {
                configurationDataMap.get(instanceName).save(writer);
            }
        }
        writer.writeEndComponents();

        writer.writeEndDocument();
        writer.close();
    }

    protected <T extends Configurable> PropertySheet<T> createPropertySheet(T configurable, ConfigurationManager cm, ConfigurationData rpd) {
        return new PropertySheet<>(configurable,cm,rpd);
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
     * @return the name given to the imported configurable.
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
                    if (!configAnnotation.redact()) {
                        if (field.get(configurable) == null) {
                            if (configAnnotation.mandatory()) {
                                throw new PropertyException(name,field.getName(),"Expected to extract a value from mandatory field, but found null");
                            } else {
                                // skip null fields, we can't extract configuration from them
                                continue;
                            }
                        }
                        FieldType ft = FieldType.getFieldType(fieldClass);
                        List<Class<?>> genericList = PropertySheet.getGenericClass(field);
                        Class<?> genericType = Object.class;
                        if (genericList.size() == 1) {
                            genericType = genericList.get(0);
                        } else if (genericList.size() == 2) {
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
                            m.put(propertyName, importCollection(genericType, name, propertyName, (Collection<?>) field.get(configurable)));
                        } else if (FieldType.arrayTypes.contains(ft)) {
                            Class<?> arrayComponentType = fieldClass.getComponentType();
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
                            @SuppressWarnings("unchecked")
                            Map<String,?> fieldMap = (Map<String,?>) field.get(configurable);
                            HashMap<String, SimpleProperty> newMap = new HashMap<>();
                            for (Map.Entry<String,?> e : fieldMap.entrySet()) {
                                String key = e.getKey();
                                Object value = e.getValue();
                                // Note this map only accepts simple fields.
                                newMap.put(key, importSimpleField(genericType, name + "-" + field.getName(), key, value));
                            }
                            m.put(propertyName, new MapProperty(newMap));
                        } else {
                            throw new PropertyException(name, "Unknown field type " +
                                    fieldClass.toString() + " found when importing " +
                                    name + " of class " + configurable.getClass().toString());
                        }
                    } else {
                        logger.log(Level.FINER, "Redacting field %s, class=%s, configurable? %s; genericType=%s configurable? %s",
                                new Object[]{field.getName(),
                                        fieldClass.getCanonicalName(),
                                        Configurable.class.isAssignableFrom(fieldClass),
                                });
                    }
                }
                field.setAccessible(accessible);
            }
            ConfigurationData rpd = new ConfigurationData(name, confClass.getName(), m);

            PropertySheet<? extends Configurable> ps = createPropertySheet(configurable, this, rpd);
            symbolTable.put(name, ps);
            configurationDataMap.put(name, rpd);
            configuredComponents.put(new ConfigWrapper(configurable), ps);
            return name;
        } catch (PropertyException ex) {
            throw ex;
        } catch (RuntimeException | IllegalAccessException ex) {
            throw new PropertyException(ex, name, propertyName,
                    String.format("Error importing %s for propName %s",
                            name, propertyName));
        }
    }

    private SimpleProperty importSimpleField(Class<?> type, String prefix, String fieldName, Object input) {
        if (Configurable.class.isAssignableFrom(type)) {
            String newName = prefix + "-" + fieldName;
            return new SimpleProperty(importConfigurable((Configurable) input, newName));
        } else if (Random.class.isAssignableFrom(type)) {
            return new SimpleProperty("" + ((Random) input).nextInt());
        } else {
            return new SimpleProperty(input.toString());
        }
    }

    private ListProperty importCollection(Class<?> innerType, String prefix, String fieldName, Collection<?> input) {
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
     * Gets the current MBean server, creating one if necessary.
     * @return the current MBean server, or <code>null</code> if there isn't
     * one available.
     */
    protected MBeanServer getMBeanServer() {
        if(mbs == null) {
            mbs = ManagementFactory.getPlatformMBeanServer();
        }
        return mbs;
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
