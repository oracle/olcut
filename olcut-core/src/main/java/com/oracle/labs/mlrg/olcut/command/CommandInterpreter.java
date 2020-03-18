/*
 * Copyright 1999-2002 Carnegie Mellon University.
 * Portions Copyright 2002-2004 Sun Microsystems, Inc.
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.labs.mlrg.olcut.command;

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

import com.oracle.labs.mlrg.olcut.OLCUT;
import com.oracle.labs.mlrg.olcut.util.Util;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;


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

    private static final String MSG_COMMAND_NOT_FOUND = "ERR  CMD_NOT_FOUND";

    /**
     * Commands for this interpreter.
     */
    protected Map<String, CommandInterface> commands = new TreeMap<>();

    /**
     * Groups for this interpreter.
     */
    protected Map<String, CommandGroupInternal> commandGroups = new TreeMap<>();

    /**
     * Interpreters that have been layered on top of this one. If interpreters
     * have been layered on top of this one, they will be consulted first for
     * commands.
     */
    protected Deque<LayeredCommandInterpreter> interpreters = new LinkedList<>();

    int totalCommands = 0;

    private boolean parseQuotes = true;

    private String prompt;

    private String rawArguments;

    boolean done = false;

    private boolean trace = false;

    CommandHistory history = new CommandHistory();

    private BufferedReader in;

    private boolean inputIsFile;

    public PrintStream out;

    private String defaultCommand;

    private LineReader consoleReader = null;

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
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.system(true);
            Terminal terminal = builder.build();
            DefaultParser parser = new DefaultParser();
            parser.setEofOnUnclosedBracket(DefaultParser.Bracket.CURLY, DefaultParser.Bracket.ROUND, DefaultParser.Bracket.SQUARE);
            parser.setEofOnUnclosedQuote(true);
            LineReaderBuilder lineBuilder = LineReaderBuilder.builder();
            logger.log(Level.FINER,"jline-3!");
            String histFile = System.getProperty("user.home")
                    + File.separator
                    + ".olcut_history_5";
            String main = Util.getMainClassName();
            if(!main.isEmpty()) {
                histFile += "_" + main;
                lineBuilder.appName(main);
            }
            lineBuilder.terminal(terminal);
            lineBuilder.parser(parser);
            lineBuilder.completer(new MultiCommandArgumentCompleter(commands,interpreters));
            lineBuilder.option(LineReader.Option.EMPTY_WORD_OPTIONS, true);
            lineBuilder.option(LineReader.Option.COMPLETE_IN_WORD, true);
            lineBuilder.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true);
            lineBuilder.option(LineReader.Option.HISTORY_BEEP,false);
            lineBuilder.variable(LineReader.HISTORY_FILE,histFile);
            lineBuilder.history(new DefaultHistory());
            consoleReader = lineBuilder.build();
        } catch(IOException e) {
            logger.info("Failed to load JLine, falling back to System.in");
            in = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    public LineReader getConsoleReader() {
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
     * @param defaultCommand The default command name.
     */
    public void setDefaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    /**
     * Adds the set of standard commands
     *
     */
    private void addStandardCommands() {
        StandardCommands stdCommands = new StandardCommands();
        add(stdCommands);
        addGroup(UNGROUPED_COMMANDS_GROUP_NAME, "Commands not in other groups");
    }

    /**
     * Dumps the commands in the interpreter
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
            String help = commands.get(cmdName).getHelp();
            putResponse(String.format("%3d) %s - %s", count, cmdName, help));
            count++;
        }
        return count;
    }

    String getCommandByNumber(int which) {
        int count = 0;
        CommandGroupInternal scg = commandGroups.get(STANDARD_COMMANDS_GROUP_NAME);
        for(String cmdName : scg) {
            if(count == which) {
                return cmdName;
            }
            count++;
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
            // Make sure that the parameter types are supported.
            for (int i = 1; i < params.length; i++) {

                //
                // Check the parameter against our 
                if (!supportedMethodParameters.contains(params[i].getType())
                        && !params[i].getType().isEnum()) {
                    logger.warning(methodName
                            + " has unsupported parameter type "
                            + params[i].getType().getSimpleName());
                }
                
                //
                // If the parameter type is array of String, then it needs 
                // to be the last parameter.
                if(params[i].getType() == String[].class) {
                    if(i != params.length - 1) {
                        logger.warning(String.format("%s has String[] parameter which is not last", methodName));
                    }
                }
            }

            //
            // Also check to see that if we have optional parameters, we
            // don't have any non-optional ones after the optional ones.
            boolean foundOptional = false;
            for (Parameter p : params) {
                Optional opt = p.getAnnotation(Optional.class);
                if (foundOptional && opt == null) {
                    logger.warning(methodName
                            + " has non-optional parameter following optional parameter.");
                    continue;
                }
                if (opt != null) {
                    foundOptional = true;
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
            if (!cmd.alias().isEmpty()) {
                addAlias(m.getName(), cmd.alias());
            }
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
    
    private HashSet<Class<?>> supportedMethodParameters =
            new HashSet<>(Arrays.asList(
                    String.class,
                    String[].class,
                    Integer.class,
                    int.class,
                    Short.class,
                    short.class,
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
                // Do not require the last argument if it's String[].
                Parameter p = params[params.length-1];
                if (p.getType() == String[].class) {
                    minParams--;
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
                            "Incorrect number of arguments.  Found %d, expected %d: %s%nUsage: %s",
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
                    if (opt != null) {
                        arg = opt.val();
                    } else {
                        arg = "";
                    }
                } else {
                    arg = args[i];
                }
                Class<?> currParam = params[i].getType();
                try {
                    if (arg.equals("<null>")) {
                        invokeParams[i] = null;
                    } else if (currParam == String.class) {
                        invokeParams[i] = arg;
                    } else if (currParam == String[].class) {
                        if (numArgs > args.length) {
                            //
                            // Insert empty string array.
                            invokeParams[i] = new String[0];
                        } else {
                            //
                            // An array of string pulls the rest of the arguments.
                            invokeParams[i] = Arrays.copyOfRange(args, i, args.length);
                        }
                        break;
                    } else if (currParam == Integer.class || currParam == int.class) {
                        invokeParams[i] = Integer.parseInt(arg);
                    } else if (currParam == Short.class || currParam == short.class) {
                        invokeParams[i] = Short.parseShort(arg);
                    } else if (currParam == Long.class || currParam == long.class) {
                        invokeParams[i] = Long.parseLong(arg);
                    } else if (currParam == Float.class || currParam == float.class) {
                        invokeParams[i] = Float.parseFloat(arg);
                    } else if (currParam == Double.class || currParam == double.class) {
                        invokeParams[i] = Double.parseDouble(arg);
                    } else if (currParam.isEnum()) {
                        @SuppressWarnings("unchecked") //Enum cast guarded by isEnum check
                        Object tmp = Enum.valueOf((Class<Enum>)currParam, arg.toUpperCase());
                        invokeParams[i] = tmp;
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
                            "Invalid value (%s) for parameter of type %s%nUsage: %s",
                            Arrays.toString(args),
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
    public void add(Map<String,CommandInterface> newCommands) {
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
        close();
        out.println("----------\n");
        if(out != System.out) {
            out.close();
        }
    }

    /**
     * Execute the given command.
     *
     * @param args	command args, args[0] contains name of cmd.
     * @return The output produced by the command.
     */
    public String execute(String[] args) {
        return execute(args, true);
    }

    /**
     * Execute the given command.
     *
     * @param args	command args, args[0] contains name of cmd.
     * @return The output produced by the command.
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
                ci = commands.get(args[0]);
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
                ci = commands.get(args[0]);
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
                response = MSG_COMMAND_NOT_FOUND;
            }

            totalCommands++;
        }
        return response;
    }

    /**
     * Execute the given command string.
     *
     * @param cmdString the command string.
     * @return The output produced by the command.
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
        List<String> words = new ArrayList<>();
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
                //printPrompt();
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
            } catch (EndOfFileException e) {
                // User terminated the session.
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
     * last command which ^old^new^ type replacements
     *
     * @return the next history line or null if done
     */
    private String getInputLine() throws IOException {
        if(consoleReader != null) {
            logger.log(Level.FINER,"In the right place");
            String message = consoleReader.readLine(prompt);
            logger.log(Level.FINER, "Read line");
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
                putResponse("bad substitution syntax, use ^old^new^");
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
            (consoleReader.getHistory()).save();
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
        return load(new File(filename));
    }

    public boolean load(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String inputLine;

            while((inputLine = br.readLine()) != null) {
                String response = CommandInterpreter.this.execute(inputLine);
                if(!response.equals("OK")) {
                    putResponse(response);
                }
                if (response.equals(MSG_COMMAND_NOT_FOUND)) {
                    putResponse("Failed to find a command, stopping.");
                    break;
                }
            }
            return true;
        } catch(IOException ioe) {
            return false;
        }
    }

    public boolean pload(String filename, int numThreads) {
        return pload(new File(filename),numThreads);
    }

    public boolean pload(File file, int numThreads) {
        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String inputLine;

            while((inputLine = br.readLine()) != null) {
                final String currLine = inputLine;
                Callable<Boolean> cmd = () -> {
                    String response = CommandInterpreter.this.execute(currLine);
                    if(!response.equals("OK")) {
                        putResponse(response);
                    }
                    return !response.equals(MSG_COMMAND_NOT_FOUND);
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
        }
    }

    /**
     * Sets the prompt for the interpreter
     *
     * @param prompt the prompt.
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Gets the prompt for the interpreter
     *
     * @return the prompt.
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * manual tester for the command interpreter.
     * @param args Command line arguments.
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

        private List<String> history = new ArrayList<>(100);

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
                return history.get((history.size() - 1) - offset);
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
                return history.get(which);
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

    protected static class CommandGroupInternal implements Iterable<String> {

        private String groupName;

        private String description;

        private Set<String> commands;

        public CommandGroupInternal(String groupName, String description, boolean keepSorted) {
            this.groupName = groupName;
            this.description = description;
            if(keepSorted) {
                commands = new TreeSet<>();
            } else {
                commands = new LinkedHashSet<>();
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

    /**
     * Default commands added to all command shells.
     */
    class StandardCommands implements CommandGroup {

        @Command(usage = "lists available commands")
        public String help(CommandInterpreter ci) {
            ci.dumpCommands();
            return "";
        }

        @Command(usage = "shows command history")
        public String history(CommandInterpreter ci) {
            ci.history.dump();
            return "";
        }

        @Command(usage = "shows command status")
        public String status(CommandInterpreter ci) {
            ci.putResponse("Total number of commands: " + ci.totalCommands);
            return "";
        }

        @Command(usage = "Echos the input back after the line processing")
        public String echo(CommandInterpreter ci, String[] args) {
            StringBuilder b = new StringBuilder(80);

            for (int i = 1; i < args.length; i++) {
                b.append(args[i]);
                b.append(" ");
            }
            ci.putResponse(b.toString());
            return "";
        }

        @Command(usage = "Print the args")
        public String pargs(CommandInterpreter ci, String[] args) throws Exception {
            ci.putResponse(String.format("args: %s", Arrays.toString(args)));
            return "";
        }

        @Command(usage = "Execute a command by number", alias = "m")
        public String menu(CommandInterpreter ci, int which, String[] args) {
            String cmd = ci.getCommandByNumber(which);
            if (cmd == null) {
                return "can't find that command";
            } else {
                String[] newArgs = new String[1+args.length];
                newArgs[0] = cmd;
                System.arraycopy(args,0,newArgs,1,args.length);
                return ci.execute(newArgs);
            }
        }

        @Command(usage = "exit the shell", alias = "exit")
        public String quit(CommandInterpreter ci) {
            ci.done = true;
            return "";
        }

        @Command(usage = "displays OLCUT version information")
        public String version(CommandInterpreter ci) {
            ci.putResponse("Command Interpreter - OLCUT Version " + OLCUT.VERSION);
            return "";
        }

        @Command(usage = "Suggests to the runtime it should perform garbage colletion")
        public String gc(CommandInterpreter ci) {
            Runtime.getRuntime().gc();
            return "";
        }

        @Command(usage = "shows memory statistics")
        public String memory(CommandInterpreter ci) {
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();

            ci.putResponse("Free Memory  : " + freeMem / (1024.0 * 1024) + " mbytes");
            ci.putResponse("Total Memory : " + totalMem / (1024.0 * 1024) + " mbytes");
            return "";
        }

        @Command(usage = "pauses for a given number of seconds, delay <time>")
        public String delay(CommandInterpreter ci, float time) {
            try {
                sleep((long) (time * 1000));
            } catch (InterruptedException ie) {
            }
            return "";
        }

        @Command(usage = "adds a pseudonym of shorthand term for a command", completers = "aliasCompleters")
        public String alias(CommandInterpreter ci, String alias, String cmd) {
            ci.addAlias(cmd, alias);
            return "";
        }

        public Completer commandCompleter = new CommandCompleter(commands, interpreters);

        public Completer[] aliasCompleters() {
            return new Completer[]{
                    new NullCompleter(),
                    commandCompleter
            };
        }

        @Command(usage = "repeatedly execute a command, repeat <int> <command> <args>")
        public String repeat(CommandInterpreter ci, int count, String[] args) {
            if (args.length >= 1) {
                for (int i = 0; i < count; i++) {
                    ci.putResponse(ci.execute(args));
                }
            } else {
                ci.putResponse("Usage: repeat count command args");
            }
            return "";
        }

        public Completer[] repeatCompleters() {
            return new Completer[]{
                    new NullCompleter(),
                    commandCompleter
            };
        }

        @Command(usage = "Redirect a single command to a file, redirect <file-name> <command> <args>")
        public String redirect(CommandInterpreter ci, File outputFile, String[] args) {
            try {
                PrintStream oldOut = ci.out;
                ci.out = new PrintStream(outputFile, "utf-8");
                ci.putResponse(ci.execute(args));
                ci.out.close();
                ci.out = oldOut;
            } catch (IOException ioe) {
                System.err.println("Can't write to " + args[1] + " " + ioe);
            }
            return "";
        }

        public Completer[] redirectCompleters() {
            return new Completer[] {
                    new Completers.FileNameCompleter(),
                    commandCompleter
            };
        }

        @Command(usage="Load and execute commands from a file", completers="filenameCompleters")
        public String load(CommandInterpreter ci, File file) {
            if(!ci.load(file)) {
                ci.putResponse("load: trouble loading " + file.toString());
            }
            return "";
        }

        @Command(usage="load and execute commands from a file in parallel, pload <file> <numThreads>",completers="filenameCompleters")
        public String pload(CommandInterpreter ci, File file, int numThreads) {
        if (numThreads < 1) {
            ci.putResponse("pload: supply a positive number of threads, recieved " + numThreads);
        }
            if(!ci.pload(file, numThreads)) {
                ci.putResponse("pload: trouble loading " + file.toString());
            }
            return "";
        }

        @Command(usage="execute multiple commands on a single line, each command separated by ';'")
        public String chain(CommandInterpreter ci, String... args) {
            if(args.length > 1) {
                String[] subargs = new String[args.length - 1];
                List<String[]> commands = new ArrayList<>(5);
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

                for(String[] i : commands) {
                    ci.putResponse(ci.execute(i));
                }
            } else {
                ci.putResponse("Usage: chain cmd1 ; cmd2 ; cmd3 ");
            }
            return "";
        }

        public Completer[] timeCompleters() {
            return new Completer[]{commandCompleter};
        }

        @Command(usage="report the time it takes to run a commmand",completers="timeCompleters")
        public String time(CommandInterpreter ci, String[] args) {
            long startTime = System.currentTimeMillis();
            long endTime;

            ci.putResponse(ci.execute(args));
            endTime = System.currentTimeMillis();

            ci.putResponse("Time: " + ((endTime - startTime) / 1000.0) + " seconds");
            return "";
        }

        @Command(usage="report the time it takes to run a command in milliseconds",completers="timeCompleters")
        public String mstime(CommandInterpreter ci, String[] args) {
            if(args.length > 1) {
                long startTime = System.nanoTime();
                ci.putResponse(ci.execute(args));
                ci.putResponse(String.format("Time: %.3f ms", (System.nanoTime() - startTime) / 1000000.0));
            } else {
                ci.putResponse("Usage: mstime cmd [args]");
            }
            return "";
        }

        @Command(usage="Redirects all subsequent commands to the given file.",completers="filenameCompleters")
        public String setoutput(CommandInterpreter ci, File outputFile) throws Exception {
            ci.out = new PrintStream(outputFile, "utf-8");
            return "";
        }

        public Completer[] filenameCompleters() {
            return new Completer[]{
                    new Completers.FileNameCompleter()
            };
        }

        @Command(usage="resets the output to be System.out")
        public String unredir(CommandInterpreter ci) {
            if(ci.out != System.out) {
                ci.out.close();
            }
            ci.out = System.out;
            return "";
        }

        @Override
        public String getName() {
            return STANDARD_COMMANDS_GROUP_NAME;
        }

        @Override
        public String getDescription() {
            return "Standard commands";
        }
    }
}
