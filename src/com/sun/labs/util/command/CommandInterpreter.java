/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package com.sun.labs.util.command;

import com.sun.labs.util.Utilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jline.console.completer.Completer;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import jline.console.history.History;
import jline.console.completer.NullCompleter;
import jline.console.history.FileHistory;

/**
 * This class is a command interpreter. It reads strings from an input stream,
 * parses them into commands and executes them, results are sent back on the
 * output stream.
 *
 * @see CommandInterpreter
 */
public class CommandInterpreter extends Thread {

    private static final Logger logger = Logger.getLogger(CommandInterpreter.class.getName());

    public static final String STANDARD_COMMANDS_GROUP_NAME = "Standard";

    public static final String UNGROUPED_COMMANDS_GROUP_NAME = "Ungrouped";

    /**
     * Commands for this interpreter.
     */
    protected Map<String, CommandInterface> commands = new TreeMap();

    /**
     * Groups for this interpreter.
     */
    protected Map<String, CommandGroupInternal> commandGroups = new TreeMap();

    /**
     * Interpreters that have been layered on top of this one. If interpreters
     * have been layered on top of this one, they will be consulted first for
     * commands.
     */
    protected Deque<LayeredCommandInterpreter> interpreters = new LinkedList<LayeredCommandInterpreter>();

    private int totalCommands = 0;

    private boolean parseQuotes = true;

    private String prompt;

    private String rawArguments;

    private boolean done = false;

    private boolean trace = false;

    private CommandHistory history = new CommandHistory();

    private BufferedReader in;

    private boolean inputIsFile;

    public PrintStream out;

    private String defaultCommand;

    private ConsoleReader consoleReader = null;

    private Pattern layeredCommandPattern = Pattern.compile("(.*)\\.([^.]*)");

    public CommandInterpreter(String inputFile) throws java.io.IOException {
        addStandardCommands();
        if(inputFile == null) {
            setupJLine();
        } else {
            in = new BufferedReader(new FileReader(inputFile));
            inputIsFile = true;
        }
        out = System.out;
    }

    /**
     * Creates a command interpreter that won't read a stream.
     *
     */
    public CommandInterpreter() {
        addStandardCommands();
        setupJLine();
        out = System.out;
    }

    protected void setupJLine() {
        try {
            consoleReader = new ConsoleReader();
            consoleReader.setBellEnabled(false);
            String histFile = System.getProperty("user.home")
                    + File.separator
                    + ".olcut_history";
            String main = Utilities.getMainClassName();
            if(!main.isEmpty()) {
                histFile += "_" + main;
            }
            History history
                    = new FileHistory(new File(histFile));
            consoleReader.setHistory(history);
            //consoleReader.setDebug(new PrintWriter(System.out));
            consoleReader.addCompleter(new MultiCommandArgumentCompleter(consoleReader, commands, interpreters));
        } catch(IOException e) {
            logger.info("Failed to load JLine, falling back to System.in");
            in = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public void setParseQuotes(boolean parseQuotes) {
        this.parseQuotes = parseQuotes;
    }

    /**
     * Sets the trace mode of the command interpreter.
     *
     * @param trace true if tracing.
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * Sets a default command to be used as a prefix for a string that isn't a
     * recognized command.
     */
    public void setDefaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    /**
     * Adds the set of standard commands
     *
     */
    private void addStandardCommands() {

        addGroup(STANDARD_COMMANDS_GROUP_NAME, "Standard commands");
        addGroup(UNGROUPED_COMMANDS_GROUP_NAME, "Commands not in other groups");

        add("help", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                dumpCommands();
                return "";
            }

            public String getHelp() {
                return "lists available commands";
            }
        });

        add("history", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                history.dump();
                return "";
            }

            public String getHelp() {
                return "shows command history";
            }
        });

        add("status", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                putResponse("Total number of commands: " + totalCommands);
                return "";
            }

            public String getHelp() {
                return "shows command status";
            }
        });

        add("echo", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                StringBuilder b = new StringBuilder(80);

                for(int i = 1; i < args.length; i++) {
                    b.append(args[i]);
                    b.append(" ");
                }
                putResponse(b.toString());
                return "";
            }

            public String getHelp() {
                return "display a line of text";
            }
        });

        add("pargs", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                putResponse(String.format("args: %s", Arrays.toString(args)));
                return "";
            }

            @Override
            public String getHelp() {
                return "Print the args";
            }
        });

        add("menu", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length < 2) {
                    return "usage: menu command-number [args]";
                } else {
                    try {
                        int which = Integer.parseInt(args[1]);
                        String cmd = getCommandByNumber(which);
                        if(cmd == null) {
                            return "can't find that command";
                        } else {
                            String[] subargs = new String[args.length - 1];
                            if(args.length > 2) {
                                System.arraycopy(args, 2, subargs,
                                        1, subargs.length - 1);
                            }
                            subargs[0] = cmd;
                            return CommandInterpreter.this.execute(subargs);
                        }
                    } catch(NumberFormatException e) {
                        return "bad number format";
                    }
                }
            }

            public String getHelp() {
                return "execute a command by number";
            }
        });

        addAlias("menu", "m");

        if(false) {
            add("argtest", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

                public String execute(CommandInterpreter ci, String[] args) {
                    StringBuffer b = new StringBuffer(80);

                    out.println("arg length is " + args.length);
                    for(int i = 0; i < args.length; i++) {
                        b.append(args[i]);
                        b.append("\n");
                    }
                    putResponse(b.toString());
                    return "";
                }

                public String getHelp() {
                    return "argument test";
                }
            });
        }

        add("quit", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                done = true;
                return "";
            }

            public String getHelp() {
                return "exit the shell";
            }
        });

        add("exit", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                done = true;
                return "";
            }

            public String getHelp() {
                return "exit the shell";
            }
        });

        add("on_exit", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                return "";
            }

            public String getHelp() {
                return "command executed upon exit";
            }
        });

        add("version", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                putResponse("Command Interpreter - Version 1.1 ");
                return "";
            }

            public String getHelp() {
                return "displays version information";
            }
        });

        add("gc", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                Runtime.getRuntime().gc();
                return "";
            }

            public String getHelp() {
                return "performs garbage collection";
            }
        });

        add("memory", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                long totalMem = Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();

                putResponse("Free Memory  : " + freeMem / (1024.0 * 1024) + " mbytes");
                putResponse("Total Memory : " + totalMem / (1024.0 * 1024) + " mbytes");
                return "";
            }

            public String getHelp() {
                return "shows memory statistics";
            }
        });

        add("delay", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length == 2) {
                    try {
                        float seconds = Float.parseFloat(args[1]);
                        Thread.sleep((long) (seconds * 1000));
                    } catch(NumberFormatException nfe) {
                        putResponse("Usage: delay time-in-seconds");
                    } catch(InterruptedException ie) {
                    }
                } else {
                    putResponse("Usage: delay time-in-seconds");
                }
                return "";
            }

            public String getHelp() {
                return "pauses for a given number of seconds";
            }
        });

        add("alias", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length == 3) {
                    String alias = args[1];
                    String cmd = args[2];
                    CommandInterpreter.this.addAlias(cmd, alias);
                } else {
                    putResponse("Usage: alias name def");
                }
                return "";
            }

            public String getHelp() {
                return "adds a pseudonym or shorthand term for a command";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new NullCompleter(),
                    new CommandCompleter(commands, interpreters),
                    new NullCompleter()
                };
            }
        });

        add("repeat", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length >= 3) {
                    try {
                        int count = Integer.parseInt(args[1]);
                        String[] subargs = new String[args.length - 2];
                        System.arraycopy(args, 2, subargs, 0, subargs.length);

                        for(int i = 0; i < count; i++) {
                            putResponse(
                                    CommandInterpreter.this.execute(subargs));
                        }
                    } catch(NumberFormatException nfe) {
                        putResponse("Usage: repeat count command args");
                    }
                } else {
                    putResponse("Usage: repeat count command args");
                }
                return "";
            }

            public String getHelp() {
                return "repeatedly execute a command";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new NullCompleter(),
                    new CommandCompleter(commands, interpreters),
                    new NullCompleter()
                };
            }
        });

        add("redirect", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length >= 3) {
                    try {
                        String[] subargs = new String[args.length - 2];
                        System.arraycopy(args, 2, subargs, 0, subargs.length);
                        PrintStream oldOut = out;
                        out = new PrintStream(args[1], "utf-8");
                        putResponse(CommandInterpreter.this.execute(subargs));
                        out.close();
                        out = oldOut;
                    } catch(IOException ioe) {
                        System.err.println("Can't write to " + args[1] + " " + ioe);
                    }
                } else {
                    putResponse("Usage: redirect file command [args ...]");
                }
                return "";
            }

            public String getHelp() {
                return "redirect command output to a file";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new FileNameCompleter(),
                    new CommandCompleter(commands, interpreters),
                    new NullCompleter()
                };
            }
        });

        add("load", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length == 2) {
                    if(!load(args[1])) {
                        putResponse("load: trouble loading " + args[1]);
                    }
                } else {
                    putResponse("Usage: load filename");
                }
                return "";
            }

            public String getHelp() {
                return "load and execute commands from a file";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new FileNameCompleter(),
                    new NullCompleter()
                };
            }
        });

        add("pload", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length == 3) {
                    if(!pload(args[1], Integer.parseInt(args[2]))) {
                        putResponse("pload: trouble loading " + args[1]);
                    }
                } else {
                    putResponse("Usage: pload <filename> <numThreads>");
                }
                return "";
            }

            public String getHelp() {
                return "load and execute commands from a file in parallel";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new FileNameCompleter(),
                    new NullCompleter()
                };
            }
        });

        add("chain", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length > 1) {
                    String[] subargs = new String[args.length - 1];
                    List commands = new ArrayList(5);
                    int count = 0;
                    for(int i = 1; i < args.length; i++) {
                        if(args[i].equals(";")) {
                            if(count > 0) {
                                String[] trimmedArgs = new String[count];
                                System.arraycopy(subargs, 0, trimmedArgs,
                                        0, trimmedArgs.length);
                                commands.add(trimmedArgs);
                                count = 0;
                            }
                        } else {
                            subargs[count++] = args[i];
                        }
                    }

                    if(count > 0) {
                        String[] trimmedArgs = new String[count];
                        System.arraycopy(subargs, 0, trimmedArgs,
                                0, trimmedArgs.length);
                        commands.add(trimmedArgs);
                        count = 0;
                    }

                    for(Iterator i = commands.iterator(); i.hasNext();) {
                        putResponse(CommandInterpreter.this.execute(
                                (String[]) i.next()));
                    }
                } else {
                    putResponse("Usage: chain cmd1 ; cmd2 ; cmd3 ");
                }
                return "";
            }

            public String getHelp() {
                return "execute multiple commands on a single line";
            }
        });

        add("time", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length > 1) {
                    String[] subargs = new String[args.length - 1];
                    System.arraycopy(args, 1, subargs, 0, subargs.length);
                    long startTime = System.currentTimeMillis();
                    long endTime;

                    putResponse(CommandInterpreter.this.execute(subargs));
                    endTime = System.currentTimeMillis();

                    putResponse("Time: " + ((endTime - startTime) / 1000.0) + " seconds");

                } else {
                    putResponse("Usage: time cmd [args]");
                }
                return "";
            }

            public String getHelp() {
                return "report the time it takes to run a command";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new CommandCompleter(commands, interpreters),
                    new NullCompleter()
                };
            }
        });

        add("mstime", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length > 1) {
                    String[] subargs = new String[args.length - 1];
                    System.arraycopy(args, 1, subargs, 0, subargs.length);
                    long startTime = System.nanoTime();
                    putResponse(CommandInterpreter.this.execute(subargs));
                    putResponse(String.format("Time: %.3f ms", (System.nanoTime() - startTime) / 1000000.0));
                } else {
                    putResponse("Usage: time cmd [args]");
                }
                return "";
            }

            public String getHelp() {
                return "report the time it takes to run a command";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new CommandCompleter(commands, interpreters),
                    new NullCompleter()
                };
            }
        });

        add("redir", STANDARD_COMMANDS_GROUP_NAME, new CompleterCommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if(args.length != 2) {
                    return "redir <output file>";
                }
                out = new PrintStream(args[1], "utf-8");
                return "";
            }

            @Override
            public String getHelp() {
                return "redirects the output stream to the given file";
            }

            @Override
            public Completer[] getCompleters() {
                return new Completer[]{
                    new FileNameCompleter(),
                    new NullCompleter()
                };
            }
        });

        add("unredir", STANDARD_COMMANDS_GROUP_NAME, new CommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if(out != System.out) {
                    out.close();
                }
                out = System.out;
                return "";
            }

            @Override
            public String getHelp() {
                return "resets the output to be System.out";
            }
        });
    }

    /**
     * Dumps the commands in the interpreter
     *
     * @param numbered if true number the commands
     *
     */
    protected void dumpCommands() {
        int count = dumpGroup(commandGroups.get(STANDARD_COMMANDS_GROUP_NAME), 0);
        for(CommandGroupInternal cg : commandGroups.values()) {
            if(cg.getGroupName().equals(STANDARD_COMMANDS_GROUP_NAME)) {
                continue;
            }
            count = dumpGroup(cg, count);
        }
        for(LayeredCommandInterpreter lci : interpreters) {
            putResponse(String.format("Commands from %s labeled with .%s", lci.getLayerName(), lci.getLayerTag()));
            lci.dumpCommands();
        }
    }

    protected int dumpGroup(CommandGroupInternal cg, int count) {
        putResponse(String.format("%s group: %s", cg.getGroupName(), cg.getDescription()));
        for(String cmdName : cg) {
            String help = ((CommandInterface) commands.get(cmdName)).getHelp();
            putResponse(String.format("%3d) %s - %s", count, cmdName, help));
            count++;
        }
        return count;
    }

    private String getCommandByNumber(int which) {
        int count = 0;
        CommandGroupInternal scg = commandGroups.get(STANDARD_COMMANDS_GROUP_NAME);
        for(String cmdName : scg) {
            if(count == which) {
                return cmdName;
            }
        }

        for(CommandGroupInternal cg : commandGroups.values()) {
            if(cg.getGroupName().equals(STANDARD_COMMANDS_GROUP_NAME)) {
                continue;
            }
            for(String cmdName : cg) {
                if(count == which) {
                    return cmdName;
                }
                count++;
            }
        }
        return null;
    }

    public void addGroup(String groupName, String description) {
        addGroup(groupName, description, true);
    }

    public void addGroup(String groupName, String description, boolean keepSorted) {
        CommandGroupInternal cg = commandGroups.get(groupName);
        if(cg != null) {
            return;
        }
        commandGroups.put(groupName, new CommandGroupInternal(groupName, description, keepSorted));
    }

    public void add(String commandName, String groupName, CommandInterface command) {
        if(groupName == null) {
            groupName = UNGROUPED_COMMANDS_GROUP_NAME;
        }
        commands.put(commandName, command);
        CommandGroupInternal cg = commandGroups.get(groupName);
        if(cg == null) {
            logger.warning(String.format("Unknown command group name: %s (from %s)", groupName, commandName));
            cg = new CommandGroupInternal(groupName, "", false);
            commandGroups.put(groupName, cg);
        }
        cg.add(commandName);
    }

    /**
     * Adds the given command to the command list.
     *
     * @param name	the name of the command.
     * @param command	the command to be executed.
     *
     */
    public void add(String name, CommandInterface command) {
        add(name, null, command);
    }

    /**
     * Adds commands for all the methods in the provided object that are
     * annotated with an @Command annotation
     * 
     * @param group an object with commands in it
     */
    public void add(final CommandGroup group) {
        //
        // Add a group for this CommandGroup
        addGroup(group.getName(), group.getDescription());
        
        //
        // Look at all the methods and find the annotated ones, if any
        Method[] methods = group.getClass().getMethods();
        for (final Method m : methods) {
            String methodName = group.getClass().getName() + "#" + m.getName();
            final Command cmd = m.getAnnotation(Command.class);
            if (cmd == null) {
                continue;
            }

            //
            // Let's see if the params are of acceptable types
            Parameter[] params = m.getParameters();
            if (params.length == 0 || params[0].getType() != CommandInterpreter.class) {
                logger.warning(methodName +
                        " must have CommandInterpreter for its first parameter");
                continue;
            }
            //
            // If we aren't getting just a string array, check for supported
            // types for our other params
            if (!(params.length == 2 && params[1].getType() == String[].class)) {
                for (int i = 1; i < params.length; i++) {
                    if (!supportedMethodParameters.contains(params[i].getType())
                            && !params[i].getType().isEnum()) {
                        logger.warning(methodName
                                + " has unsupported parameter type "
                                + params[i].getType().getSimpleName());
                    }
                }
                
                //
                // Also check to see that if we have optional parameters, we
                // don't have any non-optional ones after the optional ones.
                boolean foundOptional = false;
                for (Parameter p : params) {
                    Optional opt = p.getAnnotation(Optional.class);
                    if (foundOptional && opt == null) {
                        logger.warning(methodName +
                                " has non-optional parameter following optional parameter.");
                        continue;
                    }
                    if (opt != null) {
                        foundOptional = true;
                    }
                }
                
            }

            //
            // And check return type
            if (m.getReturnType() != String.class) {
                logger.warning(methodName +
                        " has wrong return type.  Expected String");
                continue;
            }
            
            
            //
            // Now let's see if there's a method to get the completers for
            // this method.  First, check for an explicit one in the
            // annotation itself.
            Method completorMtd = null;
            if (!cmd.completers().isEmpty()) {
                try {
                    completorMtd = group.getClass().getMethod(cmd.completers(), (Class<?>[])null);
                } catch (NoSuchMethodException e) {
                    logger.warning(methodName +
                            " references a non-existant completor method: "
                            + cmd.completers());
                }
            }
            
            //
            // If we didn't get a completor that way, try to see if there's
            // a method with a name that conforms to the convention.
            try {
                completorMtd = group.getClass().getMethod(m.getName() + "Completors", (Class<?>[])null);
            } catch (NoSuchMethodException e) {
                //
                // This is okay, probably they just didn't want a completor.
                logger.finer(methodName + " has no completors");
            }
            
            //
            // Get a completor for this method and add it in to our shell
            CommandInterface ci = methodToCommand(m, cmd.usage(), group, completorMtd);
            add(m.getName(), group.getName(), ci);
        }
    }
    
    private CommandInterface methodToCommand(final Method m,
                                             final String usage,
                                             final CommandGroup group,
                                             final Method cm) {
        if (cm != null) {
            //
            // Make sure the return type is right since we haven't checked
            // that yet.
            if (cm.getReturnType() != Completer[].class) {
                logger.warning(group.getClass().getName() + "#" + cm.getName() +
                        " has wrong return type.  Expected Completor[]");
            }
            //
            // Make a CompletorCommand instead of a regular one
            CommandInterface ci = new CompleterCommandInterface() {
                @Override
                public String execute(CommandInterpreter ci, String[] args)
                        throws Exception {
                    return invokeMethod(m, usage, group, ci, args);
                }
                
                @Override
                public String getHelp() {
                    return usage;
                }
                
                @Override
                public Completer[] getCompleters() {
                    try {
                        return (Completer[])cm.invoke(group);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.log(Level.WARNING, "Couldn't invoke "
                                + group.getClass().getName() + "#" + cm.getName(), e);
                        return null;
                    }
                }
            };
            return ci;
        } else {
            //
            // Create a regular CommandInterface for it and add it in.
            CommandInterface ci = new CommandInterface() {
                @Override
                public String execute(CommandInterpreter ci, String[] args)
                        throws Exception {
                    return invokeMethod(m, usage, group, ci, args);
                }
                
                @Override
                public String getHelp() {
                    return usage;
                }
            };
            return ci;
        }
        
    }
    
    
    
    private HashSet<Class> supportedMethodParameters =
            new HashSet<Class>(Arrays.asList(
                    String.class,
                    Integer.class,
                    int.class,
                    Long.class,
                    long.class,
                    Float.class,
                    float.class,
                    Double.class,
                    double.class,
                    Boolean.class,
                    boolean.class,
                    File.class));
    
    /**
     * Actually invoke the method for a command.  Tries to parse command
     * line arguments based on the method parameters or just passes straight
     * through if the argument after the CommandIntereter is String[].
     * This method only supports specific types.  If you add a type here,
     * please add it to the list of supported types above.
     * @param m
     * @param usage
     * @param group
     * @param ci
     * @param args
     * @return
     * @throws Exception 
     */
    private String invokeMethod(Method m,
                                String usage,
                                CommandGroup group,
                                CommandInterpreter ci,
                                String[] args) throws Exception {
        //
        // Check the params to see how we should invoke the method
        Parameter[] params = m.getParameters();
        //
        // First param is the interpreter, next param is either String[] args
        // or some specific parameters (or no parameters if there's no args)
        if (params.length > 1 && params[1].getType() == String[].class && m.getParameterCount() == 2) {
            //
            // Regular invocation, we'll pass through the String array
            return (String)m.invoke(group, ci, Arrays.copyOfRange(args, 1, args.length));
        } else {
            //
            // Do we have the right number of args to fill the method?
            if (m.getParameterCount() != args.length) {
                //
                // Check to see if we're off by optional parameters
                int minParams = 0;
                for (Parameter p : params) {
                    Optional opt = p.getAnnotation(Optional.class);
                    if (opt == null) {
                        minParams++;
                    }
                }
                //
                // Adjust minParams to skip the CommandInterpreter in the count
                minParams--;
                if (args.length - 1 < minParams) {
                    //
                    // Nope, got the wrong number
                    String paramStr = m.toString();
                    paramStr = paramStr.substring(paramStr.indexOf('(') + 1, paramStr.indexOf(')'));
                    if (paramStr.indexOf(',') < 0) {
                        paramStr = "<empty>";
                    } else {
                        paramStr = paramStr.substring(paramStr.indexOf(',') + 1);
                    }
                    return String.format(
                            "Incorrect number of arguments.  Found %d, expected %d: %s\nUsage: %s",
                        args.length - 1,
                        minParams,
                        paramStr,
                        usage);
                }
            }
            
            //
            // Let's get our parsing on 'cause we got some conversions to do
            int numArgs = m.getParameterCount();
            Object[] invokeParams = new Object[numArgs];
            invokeParams[0] = ci;
            for (int i = 1; i < numArgs; i++) {
                String arg;
                if (args.length - 1 < i) {
                    Optional opt = params[i].getAnnotation(Optional.class);
                    arg = opt.val();
                } else {
                    arg = args[i];
                }
                Class<?> currParam = params[i].getType();
                try {
                    if (arg.equals("null")) {
                        invokeParams[i] = null;
                    } else if (currParam == String.class) {
                        invokeParams[i] = arg;
                    } else if (currParam == Integer.class || currParam == int.class) {
                        invokeParams[i] = Integer.parseInt(arg);
                    } else if (currParam == Long.class || currParam == long.class) {
                        invokeParams[i] = Long.parseLong(arg);
                    } else if (currParam == Float.class || currParam == float.class) {
                        invokeParams[i] = Float.parseFloat(arg);
                    } else if (currParam == Double.class || currParam == double.class) {
                        invokeParams[i] = Double.parseDouble(arg);
                    } else if (currParam.isEnum()) {
                        invokeParams[i] = Enum.valueOf((Class<Enum>)currParam, arg.toUpperCase());
                    } else if (currParam == Boolean.class || currParam == boolean.class) {
                        invokeParams[i] = Boolean.parseBoolean(arg);
                    } else if (currParam == File.class) {
                        invokeParams[i] = new File(arg);
                    } else {
                        //
                        // ** NOTE: If you add supported classes, add them to the
                        // Set defined above this method called supportedMethodParamters
                        return String.format("Unsupported method argument: %s in %s#%s",
                                         currParam.getName(),
                                         group.getClass().getName(),
                                         m.getName());
                    }
                } catch (NumberFormatException e) {
                    return String.format(
                            "Invalid value (%s) for parameter of type %s\nUsage: %s",
                            args,
                            currParam.getName(),
                            usage);
                    
                }
            }
            return (String)m.invoke(group, invokeParams);
        }
        
        
    }
    
    /**
     * Adds an alias to the command
     *
     * @param command	the name of the command.
     * @param alias	the new alias
     *
     */
    public void addAlias(String command, String alias) {
        commands.put(alias, commands.get(command));
    }

    /**
     * Add the given set of commands to the list of commands.
     *
     * @param newCommands the new commands to add to this interpreter.
     */
    public void add(Map newCommands) {
        commands.putAll(newCommands);
    }

    /**
     * Adds a layered command interpreter to this command interpreter.
     *
     * @param lci the layered command interpreter to add.
     */
    public void add(LayeredCommandInterpreter lci) {
        //
        // Make sure the commands in there are set up like we are.
        lci.setOutput(out);
        lci.setParseQuotes(parseQuotes);
        lci.setTrace(trace);

        //
        // Put it on the front of the queue so that we will try this one 
        // before others.
        interpreters.addFirst(lci);
    }

    /**
     * Removes a layered command interpreter with the given layer tag.
     *
     * @param layerTag the tag for the interpreter that we want to remove.
     */
    public void remove(String layerTag) {
        for(Iterator<LayeredCommandInterpreter> i = interpreters.iterator(); i.hasNext();) {
            LayeredCommandInterpreter lci = i.next();
            if(lci.getLayerTag().equals(layerTag)) {
                i.remove();
                break;
            }
        }
    }

    /**
     * Outputs a response to the sender.
     *
     * @param response the response to send.
     *
     */
    public synchronized void putResponse(String response) {
        if(response != null && response.length() > 0) {
            out.println(response);
        }
    }

    public PrintStream getOutput() {
        return out;
    }

    public void setOutput(PrintStream out) {
        this.out = out;
    }

    /**
     * Called when the interpreter is exiting. Default behavior is to execute an
     * "on_exit" command.
     */
    protected void onExit() {
        execute("on_exit");
        for(LayeredCommandInterpreter lci : interpreters) {
            CommandInterface ci = lci.commands.get("on_exit");
            try {
                ci.execute(this, new String[]{"on_exit"});
            } catch(Exception ex) {
                logger.log(Level.SEVERE, String.format("Error on close for %s", lci.getLayerName()), ex);
            }
        }
        out.println("----------\n");
        if(out != System.out) {
            out.close();
        }
    }

    /**
     * Execute the given command.
     *
     * @param args	command args, args[0] contains name of cmd.
     */
    public String execute(String[] args) {
        return execute(args, true);
    }

    /**
     * Execute the given command.
     *
     * @param args	command args, args[0] contains name of cmd.
     */
    private String execute(String[] args, boolean first) {
        String response = "";

        CommandInterface ci = null;

        if(args.length > 0) {

            //
            // First things first: Is this a standard command? If so, we'll use
            // it from this group!
            String command = args[0];
            CommandGroupInternal cg = commandGroups.get(STANDARD_COMMANDS_GROUP_NAME);
            if(cg.contains(command)) {
                ci = (CommandInterface) commands.get(args[0]);
            }

            if(ci == null) {
                //
                // Does this command specify a layered interpreter that we should be
                // concerned about?
                Matcher m = layeredCommandPattern.matcher(command);
                if(m.matches()) {
                    String layeredName = m.group(1);
                    String layerTag = m.group(2);
                    //
                    // Find the layered interpreter with this tag name.
                    for(LayeredCommandInterpreter lci : interpreters) {
                        if(lci.getLayerTag().equals(layerTag)) {
                            String[] newArgs = Arrays.copyOf(args, args.length);
                            newArgs[0] = layeredName;
                            return lci.execute(newArgs);
                        }
                    }
                    //
                    // No match? Well, maybe it's the name of a command that 
                    // happens to match the layer pattern, so we'll just fall
                    // through here.
                }

                //
                // Check our layered interpreters first.
                for(LayeredCommandInterpreter lci : interpreters) {
                    ci = lci.commands.get(command);
                    if(ci != null) {
                        break;
                    }
                }
            }

            //
            // Now check the commands from this interpreter.
            if(ci == null) {
                ci = (CommandInterface) commands.get(args[0]);
            }
            if(ci != null) {
                try {
                    response = ci.execute(this, args);
                } catch(Exception ex) {
                    response = "ERR command exception " + ex.getMessage();
                    ex.printStackTrace(out);
                }
            } else {
                if(first && defaultCommand != null) {
                    String[] newArgs = new String[args.length + 1];
                    newArgs[0] = defaultCommand;
                    System.arraycopy(args, 0, newArgs, 1,
                            args.length);
                    return execute(newArgs, false);
                }
                response = "ERR  CMD_NOT_FOUND";
            }

            totalCommands++;
        }
        return response;
    }

    /**
     * Execute the given command string.
     *
     * @param cmdString the command string.
     *
     */
    public String execute(String cmdString) {
        if(trace) {
            out.println("Execute: " + cmdString);
        }
        if(inputIsFile) {
            out.println(cmdString);
        }
        return execute(parseMessage(cmdString));
    }

    /**
     * Parses the given message into an array of strings.
     *
     * @param message the string to be parsed.
     * @return the parsed message as an array of strings
     */
    protected String[] parseMessage(String message) {
        int tokenType;
        List<String> words = new ArrayList<String>();
        StreamTokenizer st = new StreamTokenizer(new StringReader(message));

        st.resetSyntax();
        st.whitespaceChars(0, ' ');
        st.wordChars('!', 255);

        if(parseQuotes) {
            st.quoteChar('"');
        }
        st.commentChar('#');

        while(true) {
            try {
                tokenType = st.nextToken();
                if(tokenType == StreamTokenizer.TT_WORD) {
                    words.add(st.sval);
                } else if(tokenType == '\'' || tokenType == '"') {
                    words.add(st.sval);
                } else if(tokenType == StreamTokenizer.TT_NUMBER) {
                    out.println("Unexpected numeric token!");
                } else {
                    break;
                }
            } catch(IOException e) {
                break;
            }
        }
        rawArguments = message.substring(words.get(0).length()).trim();
        return words.toArray(new String[0]);
    }

    public String getRawArguments() {
        return rawArguments;
    }

    // inherited from thread.
    public void run() {
        while(!done) {
            try {
                printPrompt();
                String message = getInputLine();
                if(message == null) {
                    break;
                } else {
                    if(trace) {
                        out.println("\n----------");
                        out.println("In : " + message);
                    }
                    message = message.trim();
                    if(message.length() > 0) {
                        putResponse(execute(message));
                    }
                }
            } catch(IOException e) {
                out.println("Exception: CommandInterpreter.run()");
                break;
            }
        }
        onExit();
    }
    // some history patterns used by getInputLine()
    private static Pattern historyPush = Pattern.compile("(.+):p");

    private static Pattern editPattern
            = Pattern.compile("\\^(.+?)\\^(.*?)\\^?");

    private static Pattern bbPattern = Pattern.compile("(!!)");

    /**
     * Gets the input line. Deals with history. Currently we support simple
     * csh-like history. !! - execute last command, !-3 execute 3 from last
     * command, !2 execute second command in history list, !foo - find last
     * command that started with foo and execute it. Also allows editing of the
     * last command wich ^old^new^ type replacesments
     *
     * @return the next history line or null if done
     */
    private String getInputLine() throws IOException {
        if(consoleReader != null) {
            String message = consoleReader.readLine();
            //
            // To support the shell's history list, show this cmd in it
            history.add(message);
            return message;
        }
        String message = in.readLine();
        if(message == null) {
            return null;
        }
        boolean justPush = false;
        boolean echo = false;
        boolean error = false;

        Matcher m = historyPush.matcher(message);
        if(m.matches()) {
            justPush = true;
            echo = true;
            message = m.group(1);
        }
        if(message.startsWith("^")) { // line editing ^foo^fum^
            m = editPattern.matcher(message);
            if(m.matches()) {
                String orig = m.group(1);
                String sub = m.group(2);
                try {
                    Pattern pat = Pattern.compile(orig);
                    Matcher subMatcher = pat.matcher(history.getLast(0));
                    if(subMatcher.find()) {
                        message = subMatcher.replaceFirst(sub);
                        echo = true;
                    } else {
                        error = true;
                        putResponse(message + ": substitution failed");
                    }
                } catch(PatternSyntaxException pse) {
                    error = true;
                    putResponse("Bad regexp: " + pse.getDescription());
                }
            } else {
                error = true;
                putResponse("bad substitution sytax, use ^old^new^");
            }
        } else if((m = bbPattern.matcher(message)).find()) {
            message = m.replaceAll(history.getLast(0));
            echo = true;
        } else if(message.startsWith("!")) {
            if(message.matches("!\\d+")) {
                int which = Integer.parseInt(message.substring(1));
                message = history.get(which);
            } else if(message.matches("!-\\d+")) {
                int which = Integer.parseInt(message.substring(2));
                message = history.getLast(which - 1);
            } else {
                message = history.findLast(message.substring(1));
            }
            echo = true;
        }

        if(error) {
            return "";
        }

        if(message.length() > 0) {
            history.add(message);
        }

        if(echo) {
            putResponse(message);
        }
        return justPush ? "" : message;
    }

    public void close() {
        try {
            ((FileHistory)consoleReader.getHistory()).flush();
        } catch (IOException e) {
            logger.log(Level.WARNING,"Failed to write history",e);
        }
        done = true;
    }

    /**
     * Prints the prompt.
     */
    private void printPrompt() {
        if(prompt != null) {
            out.print(prompt);
        }
    }

    public boolean load(String filename) {
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String inputLine;

            while((inputLine = br.readLine()) != null) {
                String response = CommandInterpreter.this.execute(inputLine);
                if(!response.equals("OK")) {
                    putResponse(response);
                }
            }
            fr.close();
            return true;
        } catch(IOException ioe) {
            return false;
        }
    }

    public boolean pload(String filename, int numThreads) {
        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
            String inputLine;

            while((inputLine = br.readLine()) != null) {
                final String currLine = inputLine;
                Callable<Void> cmd = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        String response
                                = CommandInterpreter.this.execute(currLine);
                        if(!response.equals("OK")) {
                            putResponse(response);
                        }
                        return null;
                    }
                };
                exec.submit(cmd);
            }

            exec.shutdown();
            exec.awaitTermination(1, TimeUnit.DAYS);
            return true;
        } catch(IOException ioe) {
            return false;
        } catch(InterruptedException ex) {
            logger.info("Parallel Load did not shut down properly");
            return false;
        } finally {
            try {
                br.close();
                fr.close();
            } catch(IOException ex) {
            }
        }
    }

    /**
     * Sets the prompt for the interpreter
     *
     * @param prompt the prompt.
     *
     */
    public void setPrompt(String prompt) {
        if(consoleReader != null) {
            consoleReader.setPrompt(prompt);
            this.prompt = "";
        } else {
            this.prompt = prompt;
        }
    }

    /**
     * Gets the prompt for the interpreter
     *
     * @return the prompt.
     *
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * manual tester for the command interpreter.
     *
     */
    public static void main(String[] args) {
        CommandInterpreter ci = new CommandInterpreter();

        try {
            System.out.println("Welcome to the Command interpreter test program");
            ci.setPrompt("CI> ");
            ci.run();
            System.out.println("Goodbye!");
        } catch(Throwable t) {
            System.out.println(t);
        }
    }

    class CommandHistory {

        private List history = new ArrayList(100);

        /**
         * Adds a command to the history
         *
         * @param command the command to add
         */
        public void add(String command) {
            history.add(command);
        }

        /**
         * Gets the most recent element in the history
         *
         * @param offset the offset from the most recent command
         * @return the last command executed
         */
        public String getLast(int offset) {
            if(history.size() > offset) {
                return (String) history.get((history.size() - 1) - offset);
            } else {
                putResponse("command not found");
                return "";
            }
        }

        /**
         * Gets the most recent element in the history
         *
         * @param which the offset from the most recent command
         * @return the last command executed
         */
        public String get(int which) {
            if(history.size() > which) {
                return (String) history.get(which);
            } else {
                putResponse("command not found");
                return "";
            }
        }

        /**
         * Finds the most recent message that starts with the given string
         *
         * @param match the string to match
         * @return the last command executed that matches match
         */
        public String findLast(String match) {
            for(int i = history.size() - 1; i >= 0; i--) {
                String cmd = get(i);
                if(cmd.startsWith(match)) {
                    return cmd;
                }
            }
            putResponse("command not found");
            return "";
        }

        /**
         * Dumps the current history
         *
         */
        public void dump() {
            for(int i = 0; i < history.size(); i++) {
                String cmd = get(i);
                putResponse(i + " " + cmd);
            }
        }
    }

    protected class CommandGroupInternal implements Iterable<String> {

        private String groupName;

        private String description;

        private Set<String> commands;

        public CommandGroupInternal(String groupName, String description, boolean keepSorted) {
            this.groupName = groupName;
            this.description = description;
            if(keepSorted) {
                commands = new TreeSet<String>();
            } else {
                commands = new LinkedHashSet<String>();
            }
        }

        public Set<String> getCommands() {
            return commands;
        }

        public String getDescription() {
            return description;
        }

        public String getGroupName() {
            return groupName;
        }

        public boolean contains(String commandName) {
            return commands.contains(commandName);
        }

        public void add(String commandName) {
            commands.add(commandName);
        }

        @Override
        public Iterator<String> iterator() {
            return commands.iterator();
        }
    }
}
