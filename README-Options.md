## Command line arguments

OLCUT has a parser for command line arguments. These come in
two forms: overrides for configurable fields/global properties, and options written
to a supplied struct-style Java class.

To get started, instantiate a ConfigurationManager with a reference to an options struct
and the String array of arguments.

```java
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
```

```java
    Options o = new CLOptions();
    ConfigurationManager cm = new ConfigurationManager(args,o);
    String[] unparsedArguments = cm.getUnnamedArguments();
```

Then the manager will validate that the options object doesn't have conflicting names, and
parse the arguments into the object. Any references to Configurable classes will be instantiated
after appropriate overrides have been applied. Any remaining unnamed arguments will
be stored in the configuration manager for future use. Unknown named arguments will throw
an instance of ArgumentException, as will errors with parsing etc.

The ConfigurationManager by default provides three arguments: 

* "-c" or "--config-file", which accepts a comma separated list of configuration files.
* "--usage" or "--help", which generates an exception that contains the usage message.
* "--config-file-formats", which accepts a comma separated list of FileFormatFactory implementations to be loaded before parsing the config files.
     
The usage statement is generated from the supplied Options object.
If the user supplies "--usage" or "--help" the ConfigurationManager throws UsageException
which has the usage statement as the message.

```java
    String[] usageArgs = new String[]{"--usage"};
    String[] helpArgs = new String[]{"--help"};
    
    try {
        cm = new ConfigurationManager(usageArgs,o);
    } catch (UsageException e) {
        System.out.println(e.getMessage());
        return;
    }
```

It is possible to turn off the insertion of the "-c" and "--config-file-formats" options by
supplying a flag to the ConfigurationManager on construction.

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

A valid Options object must form a tree of Options, where no option has the same character
or long name as any other. The option long name must not include whitespace, or start with '-'
or '@', and the character must be printable and not be '-' or whitespace.

### Overriding configurable fields

Overriding configurable fields and global properties has a specific syntax. Each 
argument must be of the form "--@componentname.fieldname" or "--@propertyname". This
overwrites the appropriate field in a component, throwing ArgumentException if the component
isn't found. For a global property it writes directly to the global property map, even if
that property was not defined in the configuration file.

```java
    String[] args = new String[]{"-c","/path/to/config/file.xml",
                                 "--input","/path/to/input",
                                 "--trainer","trainername",
                                 "--output","/path/to/output"
                                 "--@trainername.epochs","5"};
```

If the above arguments are supplied then the trainer object will be instantiated with
the epochs field set to the value 5.
