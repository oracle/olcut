
# The Oracle Labs (East) Configuration and Utilities Toolkit

The OLCUT provides a set of useful cross-project tools.  It has its roots in
the Sphinx 4 speech recognizer but has been significantly extended.  Functionality
is basically divided into three areas:

* A Runtime Configuration / Local and Distributed Initialization system
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
        @ConfigInteger(defaultValue=1)
        public static final String PROP_NUM_THREADS="numThreads";

        @ConfigComponentList(type=PipelineStage.class)
        public static final String PROP_STAGES="stages";

        public void newProperties(PropertySheet ps) {
            int numThreads = ps.getInt(PROP_NUM_THREADS);
            List<PipelineStage> stages = (List<PipelineStage>)ps.getComponentList(PROP_STAGES);
            [... further initialization, parameter checking, etc ...]
        }
    }

In the above example, the names of the properties are annotated with their
configuration types.  By convention, the static types are named as all caps with
underscores between words and the property names and corresponding instance
variables (if desired) are named with camel case. The config system will parse
the values in the config
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
            <property name="file" value="${outputDir}/outfile.mp3"/>
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

## Even More Options

This section describes some other useful features or patterns.

In a highly configurable system, you may wish to provide working default
configurations for most of the system in a single config file and use a
separate file for the pieces you're working on.  You can load a configuration
file as normal by instantiating a new Configuration Manager then use
cm.addProperties(URL url) to mix in the configuration in a second file.  That
file can make use of things like Global Properties and can inherit from
components in the first file.

To support a standard or default configuration, a common practice is to include
that configuration in the source tree for your project and package it in the
jar or war file. You can then use getClass().getResource(...) to retrieve
the standard configuration file from within the jar file.


## Remote Components

The configuration system can make use of Jini and RMI to help instantiate a
distributed system.  Through extentions to the component definition in the
configuration file, components can be registered as services in Jini.  When
a component lists another component as a property, that component need not
be on the same machine.  If it is remote, the configuration system will
automatically query Jini to find the service and it will return an RMI proxy
for the object.

A description of how to start a class server, run a registry, specify an
object as remote, provide ConfigurationEntries, deal with RemoteComponentManager
and RemoteMultiComponentManager, etc should be added here.  *cough*Steve*cough*



# Command Interpreter

OLCUT provides a CommandInterpreter that can be used for invoking or interacting
with your software.  It can be used as a test harness to poke and prod different
parts of your code, or can be used inside a JVM that is also running other
pieces of software that you'd like to monitor or in some other way interact
with.

The CommandInterpreter has a number of built-in commands for things like shell
history, status, file redirection, running a script of commands, etc.  These
commands are all grouped together for convenience.

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

## Optional parameters

## Tab Completion

## Manual argument processing

## Layered Command Interpreter (Mixing in more commands)

# Odds & Ends