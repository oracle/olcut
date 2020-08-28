# The Oracle Labs Configuration and Utilities Toolkit

The OLCUT is a group of utilities that facilitate making pluggable software
components with standard and interoperable command line interfaces. It has its roots in
the Sphinx 4 speech recognizer but has been significantly extended. These pieces can
be used in concert or independently:

* The Configuration System provides runtime configuration management without recompiles
* The Options Processor cleanly processes command-line arguments, including configuration changes
* The Command Interpreter provides an Annotation-based interactive shell with tab completion
* Additional Odds & Ends provide helpful utility classes

This toolkit has been used for many projects over the years and has grown to suit
the needs of a varied user-base.

# Quick Start

## Maven Coordinates
OLCUT's main components (i.e. `olcut-core`, `olcut-config-json` and `olcut-config-edn`) are available on Maven Central.

Maven:
```xml
<dependency>
    <groupId>com.oracle.labs.olcut</groupId>
    <artifactId>olcut-core</artifactId>
    <version>5.1.4</version>
</dependency>
```
or from Gradle:
```groovy
implementation 'com.oracle.labs.olcut:olcut-core:5.1.4'
```

The `olcut-extras` artifact is designed as a small tool for developers, as such you should compile the appropriate 
version based on your needs.

## Configuration System

The OLCUT [Configuration System](README-Configuration.md) uses runtime
dependency-injection to instantiate configurable components on the fly based on 
the contents of your configuration file.  It allows you to both specify the parameters
("properties") that should be given to the components at initialization time as well
as which types of objects should actually be instantiated for each component.  It
uses an XML file to describe the configuration. OLCUT uses Java Annotations extensively
to facilitate code integration.

```xml
    <config>
    <component name="myArchive" type="com.example.ArchiveImpl">
        <property name="store" value="diskStore">
        <property name="maxAgeYears" value="10">
    </component>
    
    <component name="diskStore" type="com.example.DiskStore">
        <property name="path" value="/tmp/diskStore">
    </component>
    
    <component name="dbStore" type="com.example.DatabaseStore">
        <property name="jdbcURL" value="jdbc:foodb:/connection/string">
    </component>
    </config>
```

This simple example shows how a class representing an Archive of some sort can
be parameterized via the configuration file to specify, at runtime, whether it
should use a storage mechanism on disk or in a database. The disk and database
components are also declared and can be referenced by name. Those components,
when pointing at a properly annotated class, are loaded automatically when the
Archive is loaded:

```java
    ConfigurationManager cm = new ConfiguratonManager("/path/to/my/config.xml");
    ArchiveImpl archive = (ArchiveImpl)cm.lookup("myArchive");
```

To be able to load your AchiveImpl concrete class like this, simply annotate
the appropriate fields.

```java
    public class ArchiveImpl implements Archive {
        @Config
        protected int maxAgeYears = 5;
        
        @Config(mandatory = true)
        protected Store store = null;
        
        // ...
    }
```

This is just a small sample of what the Configuration system can do. It
supports **inheritance**, many configurable types, **command line overrides**, self-description,
and multiple file formats including **JSON**.

Read all about the [Configuration System](README-Configuration.md).

## Options Processing

While technically part of the [Configuration System](README-Configuration.md),
OLCUT's Options mechanism can be used independently of it as well. It allows for
clean processing of command line arguments and fully understands the Configuration
system. While there's a lot the options processing can do, getting started with it
is pretty straightforward.

```java
    public class ArchiveMain {
        public static class ArchiveOptions implements Options {
            @Option(charName='a', longName='add', usage="add a file to the archive")
            public File fileToAdd = null;
            
            @Option(charName'd', longName='delete', usage="remove a file from the archive")
            public String fileNameToRemove = null;
        }
        
        public static void main(String[] args) throws Exception {
            ArchiveOptions opts = new ArchiveOptions();
            ConfigurationManager cm = new ConfigurationManager(args, opts);
            
            // ... check fields in opts for option values ...
        }
    }
```

The Options mechanism can automatically generate usage messages, override values
in configuration files, and supports almost all of the object types that the
Configuration System supports.

Read all about [Options Processing](README-Options.md).

## Command Interpreter

OLCUT provides a [Command Interpreter](README-Commands.md) that can be used for
invoking or interacting with your software.  It can be used as a test harness to
poke and prod different parts of your code, or can be used inside a JVM that is also
running other pieces of software that you'd like to monitor or in some other way interact
with. It is also a great way to build simple shell-based utilities without having
to make a million different main classes or command line arguments.

Start by defining some commands inside any class where they make sense:

```java
    public class ArchiveImpl implements Archive, CommandGroup {
        // ...
        
        @Command(usage="<ageInYears> - list all docs older than age")
        public String listOlderThan(CommandInterpreter ci, int years) {
            int count = 0;
            for (Doc d : store.documents()) {
                if (d.age() > years) {
                    ci.println(d.getName());
                    count++;
                }
            }
            return "Found " + count + " documents";
        }
        
        @Command(usage="<file> - add a file to the archive")
        public String add(CommandInterpreter ci, File path) {
            store.addFile(path);
            return "";
        }
    } 
```

Note that ArchiveImpl is now also a CommandGroup. CommandGroup also needs simple
methods that define its name and description, not shown.

Commands are any methods that return a String, take a CommandInterpreter as their
first argument, and take any supported primitive as additional arguments.

Start a shell that knows those commands like this:

```java
    CommandInterpreter shell = new CommandInterpreter();
    shell.setPrompt("archsh%% ");  // need to escape the %
    shell.add(archiveImplInstance);
    shell.start();
```

When you run this code, you'll get your Archive shell prompt and can type your commands:

```
    archsh% listOlderThan 5
    <output here ...>
    archsh% add /tmp/some-file.pdf
```

The Command Interpreter provides help based on your usage statements, supports custom
tab completion, many primitive types as arguments, history, readline-style editing,
optional parameters, and more.

The Command Interpreter is built around [JLine3](https://github.com/jline/jline3) and
supports the native platforms that JLine3 supports: Solaris, Linux, OS X, FreeBSD, and Windows.

Read more about the [Command Interpreter](README-Commands.md).

## Provenance

OLCUT provides a system for extracting the state of configurable objects into 
immutable Provenance objects used to record the state of a computation. It's heavily
used in [Tribuo](https://tribuo.org) to record a trainer and dataset configuration.
It can optionally include
non-configurable state. It supports conversion to a marshalled format which can be
easily serialised and deserialised from JSON.

Provenance objects can be converted back into a list of configurations, which can be 
used to recreate the config file that generated that provenance. This can be used
to recover the training configuration of an ML system from a model, and 
regenerate the model (either on new data, or with tweaked parameters).


## Odds & Ends

OLCUT provides a number of odds-and-ends utility classes that we find ourselves using
over and over. These are found in the `com.oracle.labs.mlrg.olcut.util` package.
In no particular order, they are as follows:

Utility | Description
------- | -----------
Channel, File, & IO Utils | `ChannelUtil` has helpers for interacting with `java.nio.channels`. `FileUtil` has methods for operating on directories. `IOUtil` has many many functions for building Input and OutputStreams of various kinds. These are particularly helpful for finding resources that might be in your jar, on your filesystem, or at a particular URL. Many of these methods automatically un-gzip input streams if they are gzipped.
Log Formatter | There are two `java.util.logging log formatters` (`LabsLogFormatter` and `SimpleLabsLogFormatter` that have a nice single line logging output. They also have a static method that sets all the loggers to use the appropriate formatter, which makes integrating them simpler.
LRA Cache | An extennsion of a LinkedHashMap that acts as a least recently accessed cache.
Date Parser | The CDateParser can parse dates in almost 90 different formats that we've seen, returning a Java Date object without complaining.
Getopt | Getopt is now deprecated. Use [Options Processing](README-Options.md) instead. This is still here if you need something small and simple.
Mutable Primitive Objects | Mutable types for Double, Long, and Number for use in, for example, Maps when you don't want to unbox and rebox the true primitives with every update.
Pair | It's a pair class. The fields are final and it has equals and hash code so you can use it as a key in a map or store it in a set. Having Pair here greatly reduces the number of other places you have a Pair class defined. 
Timers | `StopWatch` and `NanoWatch` provide handy timers, at millisecond or nanosecond granularity.
Sort Utilities | `SortUtil` rovides a sort function which returns the indices that the input elements should be rearranged. Very useful for finding the original position of a sorted object without zipping it yourself.
Stream Utilities | In Java 8 the stream API can run inside a Fork-Join Pool to bound the parallelism, but it does not bound the computation of the chunk size correctly. `StreamUtil` provides a bounded stream which knows how many threads are allocated, and so calculates the correct work chunk size. It also has methods for zipping two streams, and a special spliterator (`IOSpliterator`) which chunks work appropriately for reading from a IO system like a DB or a file.

## Contributing

We welcome your contributions! Have an idea? Read more about [Contributing to OLCUT](CONTRIBUTING.md).
