# OLCUT Configuration System

The OLCUT Configuration System uses runtime dependency-injection to instantiate
configurable components on the fly based on the contents of your configuration
file.  It allows you to both specify the parameters ("properties") that should
be given to the components at initialization time as well as which types of
objects should actually be instantiated for each component.  It uses an XML
file to describe the configuration. OLCUT uses Java Annotations extensively
to facilitate code integration.


## Basic Configuration / Initialization

Suppose you wish to construct a pipeline with various stages in it.  You may
want to create the pipeline with different stages depending on what task you
are performing.  Your Pipeline and its stages are represented as components
in the configuration file.

```xml
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
```

The configuration file defines an instance of a Pipeline that has two stages
in it - a LowPassFilter and an EchoCanceller.  Each of the stages has their own
parameters specified as properties.

The Pipeline class that correspond to the component would look as follow:

```java
    public class Pipeline implements Configurable {
        @Config
        private int numThreads = 1;
    
        @Config(mandatory=true)
        private List<PipelineStage> stages;
        
        private Pipeline() {}
    
        public void postConfig() {
            // ... further initialization, parameter checking, etc ...
        }
    }
```

In the above example, the properties are annotated with their
configuration types.  The config system will parse the values in the config
file to convert them to the desired type and will throw an exception if
they are not the right type.  It also checks that only parameters defined in
the object are included in the configuration file.  Properties may be tagged
as mandatory or not and may have default values (here, numThreads is given a 
default value of 1 if it is not included in the configuration file). Properties
may also be tagged "redact" which will cause their values not to appear in 
saved configuration files, or provenance objects.

To instantiate the pipeline in your code, you'd put something like the following
in your main class.

```java
    File configFile = new File("/path/to/your/config.xml");
    ConfigurationManager cm = new ConfigurationManager(configFile.toURI().toURL());
    Pipeline myPipeline = (Pipeline)cm.lookup("myPipeline");
```

The call to cm.lookup will chain-instantiate all the components, inserting the
property values into the appropriate fields, then invoke postConfig on each
component before returning the requested component.

Using this basic infrastructure, you can see how it is easy to supply multiple
configurations for a piece of software without having to recompile.  Simply
having multiple config files already allows for flexibility, but there are
other ways to further parameterize your configuration as well.  For example,
you could define multiple Pipelines in a single configuration file then have
your main class take a command-line parameter to specify the name of the
pipeline to instantiate.  Each one can refer to the same set of stages - only
the pipeline instances themselves need be duplicated.

```xml
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
```

The supported list of annotated field types are:

* Primitives
    * `boolean`, `Boolean`
    * `byte`, `Byte`
    * `short`, `Short`
    * `int`, `Integer`
    * `long`, `Long`
    * `float`, `Float`
    * `double`, `Double`
    * `String`
* Primitive array types
    * `byte[]`
    * `short[]`
    * `int[]`
    * `long[]`
    * `float[]`
    * `double[]`
* Configurable classes
    * `Configurable`
* Object array types
    * `String[]`
    * `Configurable[]`
* Generic types - the generic type must be a supported non-generic non-array type.
    * `List`
    * `EnumSet`
    * `Set`
    * `Map` //Map<String,T>
* Misc types
    * `AtomicInteger`
    * `AtomicLong`
    * `File`
    * `Path`
    * `LocalDate`
    * `OffsetTime`
    * `OffsetDateTime`
    * `URL`
    * `Random` (deprecated marked for removal in the next major release)
    * `Enum`

## Global Properties

### Getting Global Property values

In the above "main" example, the name of the pipeline to load is hard-coded
into the main program.  Rather than having to add a command-line parameter,
you might specify a global property that names which pipeline to use.

```xml
    <property name="targetPipeline" value="fancyPipeline"/>
```

Then in your main program you could use the following:

```java
    String pipelineInstance = cm.getGlobalProperty("targetPipeline");
    Pipeline pipeline = (Pipeline)cm.lookup(pipelineInstance);
```

This specific use is better met by the Options processing system described later,
but there are other uses of global properties.

### Expanding Global Property values inside a Configuration

Global properties may also be used internally in a configuration file.  For
example, perhaps a number of components should all place their output files
in the same directory, or use the same name prefix.  The path could be
defined in a global variable and used in the rest of the configuration.

```xml
    <property name="outputDir" value="/work/sound/output"/>
    
    <component name="mp3encoder" type="com.oracle.labs.sound.MP3Encoder">
        <property name="file" value="${outputDir}/outfile.au"/>
    </component>
    
    <component name="loggingStage" type="com.oracle.labs.sound.Logger">
        <property name="logFile" value="${outputDir}/pipeline.log"/>
    </component>
```

Each user can change the outputDir in a single location and have it reflected
everywhere in the file.

### Overriding Global Property values

Global Properties also correspond to Java system properties.  Any Global
Property in a configuration file can have its value overridden by a system
property without any code changes.  In the above example, the output directory
can be changed simply by specifying a system property on the command line.

```shell script
    java com.oracle.labs.sound.Processor -DoutputDir=/tmp
```

Global Properties may also be override programmatically prior to retrieving
components from a ConfigurationManager.

```java
    cm.setGlobalProperty("outputDir", "/tmp"); 
```

If you need to override single parameters, the preferable way to do this is
by using the [Options system](README-Options.md) to change them on the command line.

## Inheritance

Components may also inherit their configuration from other components and
override certain values without having to re-specify all values.  The
following component shares the configuration of the fancyPipeline above
but changes the number of processing threads.

```xml
    <component name="bigFancyPipeline" inherit="fancyPipeline">
        <property name="numThreads" value="8"/>
    </component>
```

bigFancyPipeline will use the same stages as fancyPipeline but will have more
threads.

## Serialized Objects

As a convenience (with the usual caveats about the safety, or lack thereof, of
loading serialized Java objects into your JVM), configuration files, outside of
any component, may refer to a file containing a serialized java object that is
retrievable from the `ConfigurationManaager` via the `lookupSerializedObject` method.
Refer to a serialized object (which could be in your Jar file) as follows:

```xml
    <serialized location="/full/path/to/file.gz" name="mySerializedData" type="com.oracle.labs.sound.FilterData"/>
```
Note that we've provided a GZip file here. OLCUT will recognize this and gunzip it on
the way in.

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

```xml
    <file name="more" value="more-config.xml"/>
```

This adds the new file to be processed in the XML parser. Files can override 
earlier properties.

To support a standard or default configuration, a common practice is to include
that configuration in the source tree for your project and package it in the
jar or war file. You can then use getClass().getResource(...) to retrieve
the standard configuration file from within the jar file.

## Inspecting a Configurable class

OLCUT 4.1.8 added a `DescribeConfigurable` main class which can describe a `Configurable`
class showing each configurable field along with it's type, default value, if it's
mandatory and a description of the field. It also can produce an example config
file using any loaded `ConfigWriter`. This is useful if you need to configure a class
but don't have access to it's source code.

```shell script
    java -cp classpath com.oracle.labs.mlrg.olcut.config.DescribeConfigurable -n fully.qualified.class.name -o -e xml
```

Produces a description of `fully.qualified.class.name` and an example xml file.


## Java Security Manager

The configuration and provenance systems use reflection to construct and inspect classes,
as such when running with a Java security manager you need to give the olcut jar appropriate
permissions. We have tested this set of permissions which allows the configuration and
provenance systems to work:

```
    // OLCUT permissions
    grant codeBase "file:/path/to/olcut/olcut-core.jar" {
            permission java.lang.RuntimePermission "accessDeclaredMembers";
            permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
            permission java.util.logging.LoggingPermission "control";
            permission java.io.FilePermission "<<ALL FILES>>", "read";
            permission java.util.PropertyPermission "*", "read,write";
    };
```

The read FilePermission can be restricted to the jars which contain
configuration files, configuration files on disk, and the locations of
serialised objects. The one here provides access to the complete filesystem, as
the necessary read locations are program specific. If you need to save an OLCUT
configuration out then you will also need to add write permissions for the save
location. 

The remote component loading system requires a further set of permissions which
we haven't completely captured.

## Remote Components

The configuration system can make use of Jini (a.k.a. Apache River) and RMI to help instantiate a
distributed system.  Through extensions to the component definition in the
configuration file, components can be registered as services in Jini.  When
a component lists another component as a property, that component need not
be on the same machine.  If it is remote, the configuration system will
automatically query Jini to find the service and it will return an RMI proxy
for the object.

Documentation about how to start a class server, run a registry, specify an
object as remote, provide `ConfigurationEntries`, deal with `RemoteComponentManager`
and `RemoteMultiComponentManager`, etc will eventually be added here.

OLCUT in general works fine with Java 9, but we have experienced some weird class 
loader issues when using serialization in RMI calls with Jini in another project. 
It's TBD on what the root cause is or how to fix it.

## Other config formats

OLCUT 4.1 and above support json and edn (a Clojure based format) configuration files
in addition to the standard xml format. To use one of these formats you must
register it with the ConfigurationManager before instantiation. For example:

```java
    ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
```

or 

```java
    ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
```

For the exact file format, we recommend looking at allConfig.edn or allConfig.json
in the appropriate src/test/resources directory. OLCUT supports chain loading
between all the known types of files, provided they have been registered with the
ConfigurationManager before instantiation. You'll need to add the `olcut-config-edn`
or `olcut-config-json` artifacts to your build as well.

It's possible to dynamically register a format factory at runtime by supplying 
the command line argument `--config-file-formats` which accepts a comma separated 
list of fully qualified class names which implement `FileFormatFactory`. For example:

```shell script
    java -cp classpath com.oracle.test.Test --config-file config.json --config-file-formats com.oracle.labs.mlrg.olcut.config.json.JsonConfigFactory
```

will insert the JsonConfigFactory into the configuration manager before the
configuration is loaded.
