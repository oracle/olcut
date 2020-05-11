
# The Oracle Labs (East) Configuration and Utilities Toolkit

The OLCUT provides a set of useful cross-project tools.  It has its roots in
the Sphinx 4 speech recognizer but has been significantly extended.  Functionality
is basically divided into four areas:

* A Runtime Configuration & Options parsing system
* A distributed version of the above, allowing components to be instantiated over RMI & Jini.
* A modular Command Interpreter with history and tab completion
* Odds & Ends of useful utilities

Each component may be used independently of the others.

# Configuration and Options

The OLCUT Configuration System uses runtime dependency-injection to instantiate
configurable components on the fly based on the contents of your configuration
file.  It allows you to both specify the parameters ("properties") that should
be given to the components at initialization time as well as which types of
objects should actually be instantiated for each component.  It uses an XML
file to describe the configuration. OLCUT uses Java Annotations extensively
to facilitate code integration.

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

This simple example shows how a class representing an Archive of some sort can
be parameterized via the configuration file to specify, at runtime, whether it
should use a storage mechanism on disk or in a database. The disk and database
components are also declared and can be referenced by name. Those components,
when pointing at a properly annotated class, are loaded automatically when the
Archive is loaded:

    ConfigurationManager cm = new ConfiguratonManager("/path/to/my/config.xml");
    ArchiveImpl archive = (ArchiveImpl)cm.lookup("myArchive");

To be able to load your AchiveImpl concrete class like this, simply annotate
the appropriate fields.

    public class ArchiveImpl implements Archive {
        @Config
        protected int maxAgeYears = 5;
        
        @Config(mandatory = true)
        protected Store store = null;
        
        ...
    }

This is just a small sample of what the Configuration system can do. It
supports **inheritance**, many configurable types, **command line overrides**, self-description,
a multiple file formats including **JSON**, and even remote implementation of
components over RMI via Apache River (formerly Jini).

Read all about the [Configuration System](README-Configuration.md).

# Options Processing

While technically part of the [Configuration System](README-Configuration.md),
OLCUT's Options mechanism can be used independently of it as well. It allows for
clean processing of command line arguments and fully understands the Configuration
system. While there's a lot the options processing can do, getting started with it
is pretty straightforward.

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
            
            ... check fields in opts for option values ...
        }
    }

The Options mechanism can automatically generate usage messages, override values
in configuration files, and supports almost all of the object types that the
Configuration System supports.

Read all about [Options Processing](README-Options.md).

# Command Interpreter

OLCUT provides a [Command Interpreter](README-Commands.md) that can be used for
invoking or interacting with your software.  It can be used as a test harness to
poke and prod different parts of your code, or can be used inside a JVM that is also
running other pieces of software that you'd like to monitor or in some other way interact
with. It is also a great way to build simple shell-based utilities without having
to make a million different main classes or command line arguments.

Start by defining some commands inside any class where they make sense:

    public class ArchiveImpl implements Archive, CommandGroup {
        ...
        
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
    
Note that ArchiveImpl is now also a CommandGroup. CommandGroup also needs simple
methods that define its name and description, not shown.

Commands are any methods that return a String, take a CommandInterpreter as their
first argument, and take any supported primitive as additional arguments.

Start a shell that knows those commands like this:

    CommandInterpreter shell = new CommandInterpreter();
    shell.setPrompt("archsh%% ");  // need to escape the %
    shell.add(archiveImplInstance);
    shell.start();

When you run this code, you'll get your Archive shell prompt and can type your commands:

    archsh% listOlderThan 5
    <output here ...>
    archsh% add /tmp/some-file.pdf
    
The Command Interpreter provides help based on your usage statements, supports custom
tab completion, many primitive types as arguments, history, readline-style editing,
optional parameters, and more.

Read more about the [Command Interpreter](README-Commands.md).

# Provenance

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

## Mutable primitives

For counting things in Maps when you don't want to unbox and rebox a 
long or a double with every update.

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


