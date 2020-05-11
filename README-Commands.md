# Command Interpreter

OLCUT provides a CommandInterpreter that can be used for invoking or interacting
with your software.  It can be used as a test harness to poke and prod different
parts of your code, or can be used inside a JVM that is also running other
pieces of software that you'd like to monitor or in some other way interact
with. It is also a great way to build simple shell-based utilities without having
to make a million different main classes or command line arguments.

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
CommandInterpreter may also be run as a separate thread by invoking `start()`.

Any number of parameters may be given to a Command. The CommandInterpreter will
check the number provided and give appropriate error messages including the
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
methods that have the same name (See Layered Command Interpreter below).

## Optional parameters

For more flexibility in your commands, you may specify that trailing method
parameters be optional.  Any optional parameter must have a default value
assigned to it, expressed as a string that would be entered on the command
line.  The default value may be the text "null" if you choose, although this
should only be used with object/reference types and not base types.  Optional
parameters are tagged with the `@Optional` annotation.  The above filter method
could take an optional parameter to specify where the output of the pipeline
should go:

        @Command(usage="<inFile> [<outFile>] - run this filter on a file")
        public String filter(CommandInterpreter ci,
                             File inFile, 
                             @Optional(val="/tmp/output.au", File outFile) {
            pipeline.filter(inFile, outFile);
            return "";
        }

## Tab Completion

Commands added to a `CommandInterpreter` may provide tab completion for each of
their arguments. A separate method may be used to specify how each argument should
be completed. The method returns an array of `Completer` objects (described below),
one per argument, and takes no parameters. These methods may either be associated
in the `@Command` annotation (described below), or can follow a naming convention
to be paired with the method they provide completers for. The following example
method would be used to find completers for the `filter` command:

        public Completer[] filterCompleters() {
            return new Completer[]{
                new FileNameCompleter(),
                new FileNameCompleter()
            };
        }

In this example, an array of completers is returned, one per argument.  This
could actually be simplified because the behavior of the completers is to
reuse the last completer in the array for all further arguments.  Simply
providing a single FileNameCompleter would work the same for this method.  To
prevent tab-completion for a particular parameter, or to prevent the last
completer from repeating, place a NullCompleter in the array in the appropriate
spot.

The following types of completers are available in OLCUT.  These are provided
by the [jline library](https://github.com/jline/jline3).

* FileNameCompleter - Completes file names starting in the PWD
* StringsCompleter - Pass it a list of strings or a Supplier to give it the values it should complete to
* EnumCompleter - Completes with values from a specified Enum type
* NullCompleter - Does not complete with anything.
* IntCompleter - Just kidding.  But it'd be awesome if it could figure that out, right?

If you wish to reuse a method that generates completers, you can use an
attribute of the Command annotation to specify the name of the completer method
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
                 completers="fileCompleter")

Multiple annotated commands may share the same completer method in this way.

## Manual argument processing

In some cases, taking the supported types as arguments doesn't provide enough
flexibility for the way you want your command to work.  In this case, you can
instead have your method use `String[]` as its second parameter (after the
CommandInterpreter) and all arguments provided in the shell will be passed
through verbatim to allow you to do your own argument parsing.  This is
particularly useful if you want to support a varargs-style syntax.  You may
still provide Completers even if the arguments are not otherwise specified.

## Layered Command Interpreter (Mixing in more commands)

Sometimes when building a large shell, you may have multiple CommandGroups that
provide commands with the same name. To avoid namespace collisions, you can add
your commands to a layer, then add the layer to the top-level shell.

    CommandInterpreter shell = new CommandInterpreter();
    LayeredCommandInterpreter lci = new LayeredCommandInterpreter("pipe", "Pipeline Commands");
    lci.add(processor);
    shell.add(lci);

To avoid ambiguity, all commands defined in `processor` can now be referred to
with a ".pipe" extension (e.g. `filter.pipe`), but may also be used without any
extension when there is no conflict.