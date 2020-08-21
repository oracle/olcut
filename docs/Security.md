# Security Considerations

OLCUT is a library which performs dependency injection, configuration, command
line arguments processing, and includes a full interactive CLI. However all of
these mechanisms are under the control of the developer incorporating OLCUT
into their system. For the command shell, it's important to prevent
unauthorised users from executing any command shells present in a program. For
the configuration system it's important to use the most specific types possible
for configurable fields, as this reduces the scope values that a malicious
configuration file could insert into a program, and to properly validate field
values in the `Configurable` subclass's `postConfig` method (as you would in a
normal constructor).

The configuration system is designed to prevent cyclic references, as objects
are only published once they have been fully constructed and their `postConfig`
methods have executed. It should be impossible to see a half constructed object
in any place other than that object's own `postConfig` method. If you find a
way, please raise an issue using the appropriate channels.

## Serialized files

OLCUT allows the loading of Java serialized objects as specified in the
configuration files.  Due to the inherent issues with Java serialization, these
object files should be stored in trusted locations where third parties do not
have access.  We recommend at minimum using a [JEP
290](https://openjdk.java.net/jeps/290) allowlist to prevent users from
deserializing arbitrary code, as OLCUT targets Java 8 and this is an API level
feature in Java 9+ onwards, we recommend using a process level allowlist
specified at JVM startup time. When OLCUT migrates to a newer Java version, we
will include API support for specifying the allowlist in a configuration file.

## Threat model

As a library incorporated into other programs, OLCUT expects it's inputs to be
checked by the wider program, and to have the locations it reads from
controlled appropriately.  Below we discuss a few threats specific to how OLCUT
operates.

| Threat name                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Exposed assets              | Mitigations                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Malicious configuration file | Configuration files can contain arbitrarily large trees, and could cause DOS issues. Alternatively the configuration files could be arbitrarily large and cause DOS issues when being read.                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | None                        | The developer controls which fields are configurable, and should ensure that the configurable structure is always bounded by not allowing arbitrary recursion in configurable fields. Additionally OLCUT's config storage format is flat, and will not allow circular references between configured objects. To mitigate the large config file issue we could limit the file size that is allowed to be read, though this is trickier when loading configuration from jar files. |
| Configuration saving         | Configuration files can be generated from the state of existing configurable objects (under developer control). This program state could potentially include sensitive information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | Sensitive program state     | Do not mark fields @Config if they could contain sensitive information. Alternatively if it is critical they be configurable for the program to operate, then mark them @Config(redact=true), which will prevent the sensitive fields from being written out in configuration or provenance. Finally if the information must be saved, then ensure that the file is written to secure storage.                                                                                   |
| Provenance capture           | Provenance tracks the state and construction path of objects by inspecting their fields. Each provenance object is either implemented by the developer, or autogenerated from a configurable object. If the object state contains sensitive information this will persist in the provenance.                                                                                                                                                                                                                                                                                                                                                                   | Sensitive program state     | The mitigations for configuration saving apply, along with an additional one which is to not store sensitive non-configurable fields in the provenance object implemented by the developer.                                                                                                                                                                                                                                                                                      |
| Imprecise Config annotations | The @Config annotation can be applied to a range of Java primitives and immutable classes, along with things which implement Configurable. If the developer designs a class hierarchy where the configurable fields have relaxed type boundaries (i.e. the field is of type Configurable rather than the specific type Foo) then it allows malicious configuration files to inject any other class that implements Configurable that is available on the class path. This could result in an invalid program state if not properly checked, and potentially result in unexpected behaviour as the postConfig methods are executed on the Configurable objects. | Program state and execution | @Config and @Option annotations should use the most specific field type available to them. postConfig methods should properly validate their arguments like constructors do, and be written defensively wrt to odd inputs.                                                                                                                                                                                                                                                       |

## Java Security Manager

The configuration and provenance systems use reflection to construct and
inspect classes, as such when running with a Java security manager you need to
give the olcut jar appropriate permissions. We have tested this set of
permissions which allows the configuration and provenance systems to work:

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
