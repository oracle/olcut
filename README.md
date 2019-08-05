
# The Oracle Labs (East) Configuration and Utilities Toolkit

The OLCUT provides a set of useful cross-project tools.  It has its roots in
the Sphinx 4 speech recognizer but has been significantly extended.  Functionality
is basically divided into four areas:

* A Runtime Configuration & Options parsing system
* A distributed version of the above, allowing components to be instantiated over RMI & Jini.
* A modular Command Interpreter with history and tab completion
* Odds & Ends of useful utilities

Each component may be used independently of the others.

# OLCUT Configuration System

The OLCUT Configuration System uses runtime dependency-injection to instantiate
configurable components on the fly based on the contents of your configuration
file.  It allows you to both specify the parameters ("properties") that should
be given to the components at initialization time as well as which types of
objects should actually be instantiated for each component.  It uses an XML
file describe the configuration.

## Basic Configuration / Initialization

Suppose you wish to construct a pipeline with various stages in it.  You may
want to create the pipeline with different stages depending on what task you
are performing.  Your Pipeline and its stages are represented as components
in the configuration file.

    <?xml version="1.0" encoding="UTF-8"?\>
    <config>
        <component name="myPipeline" type="com.oracle.labs.sound.Pipeline">
            <property name="numThreads" value="2"/>
            <propertylist name="stages">
                <item>lowPassStage</item>
                <item>echoStage</item>
            </propertylist>
        </component>
    
        <component name="lowPassStage" type="com.oracle.labs.sound.LowPassFilter">
            <property name="cutoff" value="2500"/>
        </component>
    
        <component name="echoStage" type="com.oracle.labs.sound.EchoCanceller">
            <property name="threshold" value="500"/>
        </component>
    </config>

The configuration file defines an instance of a Pipeline that has two stages
in it - a LowPassFilter and an EchoCanceller.  Each of the stages has their own
parameters specified as properties.

The Pipeline class that correspond to the component would look as follow:

    public class Pipeline implements Configurable {
        @Config
        private int numThreads = 1;
    
        @Config
        private List<PipelineStage> stages;
        
        private Pipeline() {}
    
        public void postConfig() {
            [... further initialization, parameter checking, etc ...]
        }
    }

In the above example, the properties are annotated with their
configuration types.  The config system will parse the values in the config
file to convert them to the desired type and will throw an exception if
they are not the right type.  It also checks that only parameters defined in
the object are included in the configuration file.  Properties may be tagged
as mandatory or not and may have default values (here, numThreads is given a 
default value of 1 if it is not included in the configuration file)

To instantiate the pipeline in your code, you'd put something like the following
in your main class.

    File configFile = new File("/path/to/your/config.xml");
    ConfigurationManager cm = new ConfigurationManager(configFile.toURI().toURL());
    Pipeline myPipeline = (Pipeline)cm.lookup("myPipeline");

The call to cm.lookup will chain-instantiate all the components and automatically
invoke the newProperties method on each, passing in the PropertySheet that is
used to get the defined property values from the configuration file.

Using this basic infrastructure, you can see how it is easy to supply multiple
configurations for a piece of software without having to recompile.  Simply
having multiple config files already allows for flexibility, but there are
other ways to further parameterize your configuration as well.  For example,
you could define multiple Pipelines in a single configuration file then have
your main class take a command-line parameter to specify the name of the
pipeline to instantiate.  Each one can refer to the same set of stages - only
the pipeline instances themselves need be duplicated.

        <component name="fancyPipeline" type="com.oracle.labs.sound.Pipeline">
            <property name="numThreads" value="2"/>
            <propertylist name="stages">
                <item>lowPassStage</item>
                <item>highPassStage</item>
                <item>echoStage</item>
                <item>volumeStage</item>
                <item>autoTuneStage</item>
            </propertylist>
        </component>
    
        <component name="dullPipeline" type="com.oracle.labs.sound.Pipeline">
            <property name="numThreads" value="2"/>
            <propertylist name="stages">
                <item>volumeStage</item>
            </propertylist>
        </component>
        
The supported list of annotated field types are:

* Primitives
    * boolean, Boolean
    * byte, Byte
    * short, Short
    * int, Integer
    * long, Long
    * float, Float
    * double, Double
    * String
* Primitive array types
    * byte[]
    * short[]
    * int[]
    * long[]
    * float[]
    * double[]
* Configurable classes
    * Configurable
* Object array types
    * String[]
    * Configurable[]
* Generic types - the generic type must be a supported non-generic non-array type.
    * List
    * EnumSet
    * Set
    * Map //Map<String,T>
* Misc types
    * AtomicInteger
    * AtomicLong
    * File
    * Path
    * LocalDate
    * OffsetTime
    * OffsetDateTime
    * Random (deprecated marked for removal in the next major release)
    * Enum

## Global Properties

### Getting Global Property values

In the above "main" example, the name of the pipeline to load is hard-coded
into the main program.  Rather than having to add a command-line parameter,
you might specify a global property that names which pipeline to use.

    <property name="targetPipeline" value="fancyPipeline"/>

Then in your main program you could use the following:

    String pipelineInstance = cm.getGlobalProperty("targetPipeline");
    Pipeline pipeline = (Pipeline)cm.lookup(pipelineInstance);

### Expanding Global Property values inside a Configuration

Global properties may also be used internally in a configuration file.  For
example, perhaps a number of components should all place their output files
in the same directory, or use the same name prefix.  The path could be
defined in a global variable and used in the rest of the configuration.

        <property name="outputDir" value="/work/sound/output"/>
    
        <component name="mp3encoder" type="com.oracle.labs.sound.MP3Encoder">
            <property name="file" value="${outputDir}/outfile.au"/>
        </component>
    
        <component name="loggingStage" type="com.oracle.labs.sound.Logger">
            <property name="logFile" value="${outputDir}/pipeline.log"/>
        </component>

Each user can change the outputDir in a single location and have it reflected
everywhere in the file.

### Overriding Global Property values

Global Properties also correspond to Java system properties.  Any Global
Property in a configuration file can have its value overridden by a system
property without any code changes.  In the above example, the output directory
can be changed simply by specifying a system property on the command line.

    java com.oracle.labs.sound.Processor -DoutputDir=/tmp

Global Properties may also be override programmatically prior to retrieving
components from a ConfigurationManager.

    cm.setGlobalProperty("outputDir", "/tmp"); 

They can also be set by using the Options system described below.

## Inheritance

Components may also inherit their configuration from other components and
override certain values without having to re-specify all values.  The
following component shares the configuration of the fancyPipeline above
but changes the number of processing threads.

        <component name="bigFancyPipeline" inherit="fancyPipeline">
            <property name="numThreads" value="8"/>
        </component>

bigFancyPipeline will use the same stages as fancyPipeline but will have more
threads.

## Other configuration aspects

This section describes some other useful features or patterns.

In a highly configurable system, you may wish to provide working default
configurations for most of the system in a single config file and use a
separate file for the pieces you're working on.  You can load a configuration
file as normal by instantiating a new Configuration Manager then use
cm.addProperties(URL url) to mix in the configuration in a second file.  That
file can make use of things like Global Properties and can inherit from
components in the first file.

It's also possible to chain load config files by adding a file tag.

        <file name="more" value="more-config.xml"/>

This adds the new file to be processed in the XML parser. Files can override 
earlier properties.

To support a standard or default configuration, a common practice is to include
that configuration in the source tree for your project and package it in the
jar or war file. You can then use getClass().getResource(...) to retrieve
the standard configuration file from within the jar file.

## Inspecting a Configurable class

OLCUT 4.1.8 added a DescribeConfigurable main class which can describe a Configurable
class showing each configurable field along with it's type, default value, if it's
mandatory and a description of the field. It also can produce an example config
file using any loaded ConfigWriter. This is useful if you need to configure a class
but don't have access to it's source code.

    java -cp classpath com.oracle.labs.mlrg.olcut.config.DescribeConfigurable -n fully.qualified.class.name -o -e xml

Produces a description of `fully.qualified.class.name`.

## Command line arguments

The configuration system has a parser for command line arguments. These come in
two forms: overrides for configurable fields/global properties, and options written
to a supplied struct.

To get started instantiate a ConfigurationManager with a reference to an options struct
and the String array of arguments.

    public static class CLOptions implements Options {
        @Override
        public String getOptionsDescription() {
            return "Command line options for loading and saving data."
        }

        @Option(charName='i',longName="input",usage="Input file")
        public File inputFile;
        @Option(charName='t',longName="trainer",usage="Trainer object")
        public Trainer trainer;
        @Option(charName='o',longName="output",usage="Output path")
        public Path outputFile;
        
        public Options otherOptions;
    }

    Options o = new CLOptions();
    ConfigurationManager cm = new ConfigurationManager(args,o);
    String[] unparsedArguments = cm.getUnnamedArguments();

Then the manager will validate that the options object doesn't have conflicting names, and
parse the arguments into the object. Any references to Configurable classes will be instantiated
after appropriate overrides have been applied. Any remaining unnamed arguments will
be stored in the configuration manager for future use. Unknown named arguments will throw
an instance of ArgumentException, as will errors with parsing etc.

The ConfigurationManager by default provides three arguments: 

* "-c" or "--config-file", which accepts a comma separated list of configuration files.
* "--usage" or "--help", which generates an exception that contains the usage message.
* "--config-file-formats", which accepts a comma separated list of FileFormatFactory implemenations to be loaded before parsing the config files.
     
The usage statement is generated from the supplied Options object.
If the user supplies "--usage" or "--help" the ConfigurationManager throws UsageException
which has the usage statement as the message.

    String[] usageArgs = new String[]{"--usage"};
    String[] helpArgs = new String[]{"--help"};
    
    try {
        cm = new ConfigurationManager(usageArgs,o);
    } catch (UsageException e) {
        System.out.println(e.getMsg());
        return;
    }

### Options objects

The supplied Options object needs to implement Options. Options is a tag interface, with
no mandatory methods. The processing system looks at the fields of the Options object, if
the field is a subclass of Options it's added to the processing queue. If the field has an
@Option annotation then the character, long name, field type and usage statement are
extracted for further processing. The @Option annotation supports all the types supported
by the config system, except for Map. Map isn't supported because seriously who wants to parse
that out of a String. List, Set and the array types are supported as comma separated lists.
The comma can be escaped by quoting or by a backslash if it's required for a String.
Configurable objects are looked up by the supplied name, a PropertyException is thrown
if the object cannot be found. It has an optional method `String getOptionsDescription()`
which can be used to insert a description of the options contained in the class for use in
the usage statement.

### Overriding configurable fields

Overriding configurable fields and global properties has a specific syntax. Each 
argument must be of the form "--@componentname.fieldname" or "--@propertyname". This
overwrites the appropriate field in a component, throwing ArgumentException if the component
isn't found. For a global property it writes directly to the global property map, even if
that property was not defined in the configuration file.

    String[] args = new String[]{"-c","/path/to/config/file.xml",
                                 "--input","/path/to/input",
                                 "--trainer","trainername",
                                 "--output","/path/to/output"
                                 "--@trainername.epochs","5"};
                                 
If the above arguments are supplied then the trainer object will be instantiated with
the epochs field set to the value 5.

## Java Security Manager

The configuration and provenance systems use reflection to construct and inspect classes,
as such when running with a Java security manager you need to give the olcut jar appropriate
permissions. We have tested this set of permissions which allows the configuration and
provenance systems to work:

    // OLCUT permissions
    grant codeBase "file:/path/to/olcut/olcut-core-5.0.0.jar" {
            permission java.lang.RuntimePermission "accessDeclaredMembers";
            permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
            permission java.util.logging.LoggingPermission "control";
            permission java.io.FilePermission "<<ALL FILES>>", "read";
            permission java.util.PropertyPermission "*", "read,write";
    };

The read FilePermission can be restricted to the jars which contain
configuration files, configuration files on disk, and the locations of
serialised objects. The one here provides access to the complete filesystem, as
the necessary read locations are program specific. If you need to save an OLCUT
configuration out then you will also need to add write permissions for the save
location. 

The remote component loading system requires a further set of permissions which
we haven't completely captured.

## Remote Components

The configuration system can make use of Jini and RMI to help instantiate a
distributed system.  Through extensions to the component definition in the
configuration file, components can be registered as services in Jini.  When
a component lists another component as a property, that component need not
be on the same machine.  If it is remote, the configuration system will
automatically query Jini to find the service and it will return an RMI proxy
for the object.

A description of how to start a class server, run a registry, specify an
object as remote, provide ConfigurationEntries, deal with RemoteComponentManager
and RemoteMultiComponentManager, etc should be added here.  *cough*Steve*cough*

OLCUT in general works fine with Java 9, but we have experienced some weird class 
loader issues when using serialization in RMI calls with Jini in another project. 
It's TBD on what the root cause is or how to fix it.

## Other config formats

OLCUT 4.1 supports json and edn (a Clojure based format) configuration files
in addition to the standard xml format. To use one of these formats you must
register it with the ConfigurationManager before instantiation. For example:

    ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());

or 

    ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());

For the exact file format, we recommend looking at allConfig.edn or allConfig.json
in the appropriate src/test/resources directory. OLCUT supports chain loading
between all the known types of files, provided they have been registered with the
ConfigurationManager before instantiation.

It's possible to dynamically register a format factory at runtime by supplying 
the command line argument "--config-file-fomats" which accepts a comma separated 
list of fully qualified class names which implement `FileFormatFactory`. For example:

    java -cp classpath com.oracle.test.Test --config-file config.json --config-file-formats com.oracle.labs.mlrg.olcut.config.json.JsonConfigFactory

will insert the JsonConfigFactory into the configuration manager before the
configuration is loaded.

# Provenance

OLCUT provides a system for extracting the state of configurable objects into 
immutable Provenance objects used to record the state of a computation. It's heavily
used in Tribuo to record a trainer and dataset configuration. It can optionally include
non-configurable state. It supports conversion to a marshalled format which can be
easily serialised and deserialised from JSON.

# Command Interpreter

OLCUT provides a CommandInterpreter that can be used for invoking or interacting
with your software.  It can be used as a test harness to poke and prod different
parts of your code, or can be used inside a JVM that is also running other
pieces of software that you'd like to monitor or in some other way interact
with.

The CommandInterpreter has a number of built-in commands for things like shell
history, status, file redirection, running a script of commands, etc.  These
commands are all grouped together for convenience, meaning they'll appear
together when you run "help".

## Defining commands

To add your own commands to the shell you simply define and annotate methods
that take a CommandInterpreter as their first argument and supported types
as any additional arguments.  Any object containing commands must implement
the CommandGroup interface.  All commands contained within the object will
be put together in the same group.

Defining a command looks like this

    public class Processor implements CommandGroup {
        public String getName() {
            return "Process";
        }
    
        public String getDescription() {
            return "Commands for the Oracle Labs Sound Processor";
        }
    
        @Command(usage="<fileName> - filter the given file")
        public String filter(CommandInterpreter ci, File file) {
            pipeline.filter(file);
            return "";
        }
    }

Then you can create and start a shell with that command in your main program,
perhaps after having loaded any relevant configuration.

    Processor p = new Processor(pipelineInstance);
    CommandInterpreter shell = new CommandInterpreter();
    shell.add(p);
    shell.run();

Your main thread will block in a read/eval/print loop at this point.
CommandInterpreter may also be run as a separate thread.

Any number of parameters may be given to a Command. The CommandInterpreter will
check the number provided and given appropriate error messages including the
usage string in the Command annotation.  It will parse the arguments and convert
them to the appropriate types and invoke the Command if possible.  Supported
argument types are:

* String
* Integer, int
* Long, long
* Double, double
* Float, float
* Boolean, boolean
* File
* Enum of any type
* String[] (as the only parameter type)

Note that there is a one-to-one correspondence between method names and
Commands.  No distinctions are made for method signatures with different
parameters.  No guarantee is made for the behavior of a shell with multiple
methods that have the same name.

## Optional parameters

For more flexibility in your commands, you may specify that trailing method
parameters be optional.  Any optional parameter must have a default value
assigned to it, expressed as a string that would be entered on the command
line.  The default value may be the text "null" if you choose, although this
should only be used with object/reference types and not base types.  Optional
parameters are tagged with the @Optional annotation.  The above filter method
could take an optional parameter to specify where the output of the pipeline
should go:

        @Command(usage="")
        public String filter(CommandInterpreter ci,
                             File inFile, 
                             @Optional(val="/tmp/output.au", File outFile) {
            pipeline.filter(inFile, outFile);
            return "";
        }

## Tab Completion

Commands added to a CommandInterpreter may provide tab completion for each of
their arguments.  A separate method may be included in the same CommandGroup
that has a name consisting of the command it is providing completors for
followed by the word Completers.  It takes no arguments and returns
`Completer[]`.

        public Completer[] filterCompleters() {
            return new Completer[]{
                new FileNameCompleter(),
                new FileNameCompleter()
            };
        }

In this example, an array of completors is returned, one per argument.  This
could actually be simplified because the behavior of the completors is to
reuse the last completor in the array for all further arguments.  Simply
providing a single FileNameCompleter would work the same for this method.  To
prevent tab-completion for a particular parameter, or to prevent the last
completor from repeating, place a NullCompleter in the array in the appropriate
spot.

The following types of completors are available in OLCUT.  Most are provided
by the [jline library](http://jline.sourceforge.net/apidocs/index.html).

* FileNameCompleter - Completes file names starting in the PWD
* SimpleCompleter - Pass it an array of strings or a Reader to give it the values it should complete to
* ClassNameCompleter - Fills in Class names, optionally with a filter applied, from your classpath
* EnumCompleter - Completes with values from a specified Enum type
* NullCompleter - Does not complete with anything.
* IntCompleter - Just kidding.  But it'd be awesome if it could figure that out, right?

If you wish to reuse a method that generates completors, you can use an
attribute of the Command annotation to specify the name of the completor method
to use instead of relying on the xxxCompleters convention.  For example,
if multiple commands take a single File parameter, you might make a method such
as this one:

        public Completers[] fileCompleter() {
            return new Completer[]{
                new FileNameCompleter(),
                new NullCompleter()
            }
        }

Then when annotating a method that takes a File as its parameter, you would
specify:

        @Command(usage="Processes a single file",
                 completors="fileCompleter")

Multiple annotated commands may share the same completor method.

## Manual argument processing

In some cases, taking the supported types as arguments doesn't provide enough
flexibility for the way you want your command to work.  In this case, you can
instead have your method use `String[]` as its second parameter (after the
CommandInterpreter) and all arguments provided in the shell will be passed
through verbatim allow you to do your own argument parsing.  This is
particularly useful if you want to support a varargs-style syntax.  You may
still provide Completers even if the arguments are not otherwise specified.

## Layered Command Interpreter (Mixing in more commands)

# Odds & Ends

## Date parser

A parser for dates in a bunch of standard formats without complaining, 
returning a Java Date object.

## Getopt

Please use the new arguments processing. This is still here if you need 
something small and stupid simple.

## Channel, File and IO utils

ChannelUtil has helpers for interacting with java.nio.channels. FileUtil has 
methods for operating on directories. IOUtil has many many functions for
building Input and OutputStreams of various kinds.

## Log formatter

There are two java.util.logging log formatters that have a nice single line 
logging output. They also have a static method that sets all the loggers to 
use the appropriate formatter, which makes integrating them simpler.

## LRACache

A least recently accessed cache.

## MutableLong

For counting things in Maps when you don't want to unbox and rebox a 
long with every update.

## Pair

It's a pair class. The fields are final and it has equals and hash code so
you can use it as a key in a map or store it in a set.

## Stop watch timer

StopWatch and NanoWatch provide timers, at millisecond or nanosecond granularity.

## Sort utils
Provides a sort function which returns the indices that the input elements
should be rearranged. Very useful for finding the original position of a
sorted object without zipping it yourself.

## Stream utils
In Java 8 the stream API can run inside a Fork-Join Pool to bound the parallelism,
but it does not bound the computation of the chunk size correctly. This class
provides a bounded stream which knows how many threads are allocated, and so 
calculates the correct work chunk size. It also has methods for zipping two 
streams, and a special spliterator which chunks work appropriately for reading
from a IO system like a DB or a file.


