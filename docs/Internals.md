# Internals

## Command Shell
The command shell allows developers to implement CommandGroup, and then
inspects the class via reflection pulling out all methods annotated @Command.
These methods are added to a CLI, with appropriate help and arguments
completion. The CLI is based on jline 3, but only provides the commands
specified by the developer, along with a set of default commands that control
how the inputs and outputs are directed, along with some help and status
commands.

The arguments for the command shell are parsed from the supplied String into
the set of supported arguments: Strings, String arrays (if the final argument
only), primitives, primitive boxes, and File.

## Configuration System
The configuration, options and provenance system provides ways for users to
configure and control their programs through a combination of command line
options and configuration files. The provenance system allows the generation of
immutable provenance objects which record configured values and optionally
runtime values depending on how the user implements the Provenance interface.

The configuration system allows developers to mark fields @Config in classes
which implement the Configurable interface. Those fields can have their values
written in on construction based on the values in the configuration file, or
presented on the command line. The supported types are primitives (and
primitive boxes), primitive arrays, strings, string arrays, classes which
implement Configurable, arrays of classes which implement Configurable, lists,
sets, maps, enums, enum sets, AtomicInteger, AtomicLong, File, Path, URL,
OffsetDateTime, LocalDate, OffsetTime, and Random (which is deprecated). The
lists and sets accept generic parameters of any of the non-collection types,
and Maps have String keys and accepted non-collection types as values. The
collection types are specified as a list of values which is written into a
concrete ArrayList, a HashSet, or a HashMap, but the annotation must be on a
field which is typed with the List, Set or Map interface.

## Lifetime of a ConfigurationManager

This summarises the lifetime of a ConfigurationManager from construction until
it goes out of scope.

1. A new ConfigurationManager is constructed
    - If it's supplied a list of file paths then those files are passed to the appropriate ConfigLoader subclass based on their file type. New subtypes can be registered by calling a static method on ConfigurationManager before instantiating it. This mechanism is how the json and edn file types are registered, the xml file type is available by default.
    - Configuration files are processed by reading the configuration into ConfigurationData objects, which are Map<String,Union<String,List<Union<String,Class>>> for each object's configuration, along with a few metadata fields like the class name, the name given in the configuration file, and if the configuration is overlaid from some other object.
2. Configurable objects can be supplied to the ConfigurationManager, these objects have their fields marked @Config inspected via reflection, and the values recorded in the configuration. This is performed recursively if the object contains Configurable object fields, or collections thereof.
3. A configuration can be looked up by name or by class.
    - If by class, each configuration which is a subclass (or the class itself) is returned as a list after they are instantiated.
    - Configured objects are instantiated as follows:
        1. The relevant class file is loaded, triggering static initialisers. Invalid or unknown classes trigger a PropertyException terminating instantiation.
        2. An instance of the class is instantiated by calling it's no-args constructor, making it accessible if necessary. If this constructor is not present then a PropertyException is triggered.
        3. Fields are initialised to their default values, as specified in the class file.
        4. Each field from the configuration is processed, making it accessible first if necessary, triggering further object instantiation if required, writing each field value into the new object. Note: this step cannot recur infinitely as the object under construction hasn't been published yet so circular references will cause PropertyException as the object cannot be found.
        5. Invalid field values trigger PropertyException, and the instantiation terminates without publishing the object.
        6. After each field has been written, it's access privileges are reset to those specified in the class.
        7. After all fields have been written then the object has it's postConfig method called. This is intended to perform object specific validation and checking, similar to a standard Java constructor. If the object is invalid it may throw PropertyException, or another RuntimeException, and the instantiated object is discarded.
        8. Finally the object is published by storing it into the ConfigurationManager's map of instantiated objects, and then returning it to the caller.
4. The current configuration can be written out to a file on disk, either including the just the objects that have been instantiated, or including all configurations known to the system (this may include configurations which are invalid, or for which the class is not available on the classpath).

## Lifetime of a ConfigurationManager processing Options

This summarises the steps executed by a `ConfigurationManager` on construction
when parsing options.

1. A class implementing Options, with fields marked @Option for values that can be read in from the command line is constructed.
2. A new ConfigurationManager is constructed, passing in the command line argument array, along with the options subclass.
3. The options subclass is validated and the usage statement constructed.
    - A valid options subclass has fields which are either options subclasses, or marked @Option and the type is one of the supported configuration types mentioned above.
    - It also requires that the charName and longName of the option fields are unique in this particular instantiation, including all fields on nested options subclasses.
4. The arguments are checked for the "help" or "usage" arguments, and a UsageException is thrown with the usage statement.
5. The arguments are checked for any which specify a config file format (i.e. a fully qualified class name which implements FileFormatFactory), the format factories are instantiated by calling their no-args constructor and passed to the ConfigurationManager's addFileFormatFactory method.
6. The arguments are checked for the configuration file list argument, and any files found are processed by the appropriate ConfigLoader subclass.
7. The arguments are checked for arguments starting "--@" and the resulting "<object-name>.<field-name> <value>" tuples are written into the configuration, overwriting or adding to what was loaded from the configuration files.
8. All arguments which match a charName or longName in the supplied options instance (and any nested options instances) have their values parsed (using the same logic as the configuration system), and assigned to the marked field. This can trigger object instantiation from the configuration, any fields which are subtypes of Configurable or collections of Configurable treat the supplied value as a configurable object name and look it up in the configuration.
9. All remaining unparsed arguments are stored in a String array for inspection from the ConfigurationManager
10. The ConfigurationManager constructor returns.
