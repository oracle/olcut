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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.History;
import jline.NullCompletor;

/**
 * This class is a command interpreter. It reads strings from an
 * input stream, parses them into commands and executes them, results
 * are sent back on the output stream.
 *
 * @see CommandInterpreter
 */
public class CommandInterpreter extends Thread {

    private static final Logger logger = Logger.getLogger(CommandInterpreter.class.getName());

    private Map<String, CommandInterface> commands;

    private Map<String, CommandGroup> commandGroups;

    private int totalCommands = 0;
    
    private boolean parseQuotes = true;

    private String prompt;

    private boolean done = false;

    private boolean trace = false;

    private CommandHistory history = new CommandHistory();

    private BufferedReader in;
    
    private boolean inputIsFile;

    public PrintStream out;

    private String defaultCommand;
    
    private ConsoleReader consoleReader = null;
    
    public CommandInterpreter(String inputFile) throws java.io.IOException {
        commands = new TreeMap();
        commandGroups = new TreeMap();
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
        commands = new TreeMap();
        commandGroups = new TreeMap();
        addStandardCommands();
        setupJLine();
        out = System.out;
    }

    protected void setupJLine() {
        try {
            consoleReader= new ConsoleReader();
            consoleReader.setBellEnabled(false);
            String histFile = System.getProperty("user.home")
                    + File.separator
                    + ".olcut_history";
            String main = Utilities.getMainClassName();
            if (!main.isEmpty()) {
                histFile += "_" + main;
            }
            History history =
                    new History(new File(histFile));
            consoleReader.setHistory(history);
            //consoleReader.setDebug(new PrintWriter(System.out));
            consoleReader.addCompletor(new MultiCommandArgumentCompletor(consoleReader, commands));
        } catch (IOException e) {
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
     *  @param trace 	true if tracing.
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * Sets a default command to be used as a prefix for a string that isn't
     * a recognized command.
     */
    public void setDefaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    /**
     * Adds the set of standard commands
     *
     */
    private void addStandardCommands() {

        addGroup("Standard", "Standard commands");
        addGroup("Ungrouped", "Commands not in other groups");

        add("help", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                dumpCommands();
                return "";
            }

            public String getHelp() {
                return "lists available commands";
            }
        });

        add("history", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                history.dump();
                return "";
            }

            public String getHelp() {
                return "shows command history";
            }
        });

        add("status", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                putResponse("Total number of commands: " + totalCommands);
                return "";
            }

            public String getHelp() {
                return "shows command status";
            }
        });

        add("echo", "Standard", new CommandInterface() {

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
        
        add("pargs", "Standard", new CommandInterface() {

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

        add("menu", "Standard", new CommandInterface() {

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
            add("argtest", "Standard", new CommandInterface() {

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

        add("quit", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                done = true;
                return "";
            }

            public String getHelp() {
                return "exit the shell";
            }
        });

        add("exit", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                done = true;
                return "";
            }

            public String getHelp() {
                return "exit the shell";
            }
        });

        add("on_exit", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                return "";
            }

            public String getHelp() {
                return "command executed upon exit";
            }
        });

        add("version", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                putResponse("Command Interpreter - Version 1.1 ");
                return "";
            }

            public String getHelp() {
                return "displays version information";
            }
        });

        add("gc", "Standard", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                Runtime.getRuntime().gc();
                return "";
            }

            public String getHelp() {
                return "performs garbage collection";
            }
        });

        add("memory", "Standard", new CommandInterface() {

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


        add("delay", "Standard", new CommandInterface() {

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

        add("alias", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new NullCompletor(),
                    new CommandCompletor(commands),
                    new NullCompletor()
                };
            }
        });

        add("repeat", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new NullCompletor(),
                    new CommandCompletor(commands),
                    new NullCompletor()
                };
            }
        });

        add("redirect", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new FileNameCompletor(),
                    new CommandCompletor(commands),
                    new NullCompletor()
                };
            }
        });

        add("load", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new FileNameCompletor(),
                    new NullCompletor()
                };
            }
        });

        add("pload", "Standard", new CompletorCommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if (args.length == 3) {
                    if (!pload(args[1], Integer.parseInt(args[2]))) {
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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new FileNameCompletor(),
                    new NullCompletor()
                };
            }
        });

        add("chain", "Standard", new CommandInterface() {

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

        add("time", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new CommandCompletor(commands),
                    new NullCompletor()
                };
            }
        });

        add("mstime", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new CommandCompletor(commands),
                    new NullCompletor()
                };
            }
        });

        add("redir", "Standard", new CompletorCommandInterface() {

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
            public Completor[] getCompletors() {
                return new Completor[] {
                    new FileNameCompletor(),
                    new NullCompletor()
                };
            }
        });

        add("unredir", "Standard", new CommandInterface() {

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
    private void dumpCommands() {
        int count = dumpGroup(commandGroups.get("Standard"), 0);
        for(CommandGroup cg : commandGroups.values()) {
            if(cg.getGroupName().equals("Standard")) {
                continue;
            }
            count = dumpGroup(cg, count);
        }
    }

    private int dumpGroup(CommandGroup cg, int count) {
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
        CommandGroup scg = commandGroups.get("Standard");
        for(String cmdName : scg) {
            if(count == which) {
                return cmdName;
            }
        }

        for(CommandGroup cg : commandGroups.values()) {
            if(cg.getGroupName().equals("Standard")) {
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
        CommandGroup cg = commandGroups.get(groupName);
        if(cg != null) {
            return;
        }
        commandGroups.put(groupName, new CommandGroup(groupName, description, keepSorted));
    }

    public void add(String commandName, String groupName, CommandInterface command) {
        if(groupName == null) {
            groupName = "Ungrouped";
        }
        commands.put(commandName, command);
        CommandGroup cg = commandGroups.get(groupName);
        if(cg == null) {
            logger.warning(String.format("Unknown command group name: %s", groupName));
            cg = new CommandGroup(groupName, "", false);
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
     * Adds an alias to the command
     *
     * @param command	the name of the command.
     * @param alias	the new aliase
     *
     */
    public void addAlias(String command, String alias) {
        commands.put(alias, commands.get(command));
    }

    /**
     * Add the given set of commands to the list
     * of commands.
     * @param newCommands 	the new commands to add to this interpreter.
     */
    public void add(Map newCommands) {
        commands.putAll(newCommands);
    }

    /**
     * Outputs a response to the sender.
     *
     * @param response 	the response to send.
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
     * Called when the interpreter is exiting. Default behavior is
     * to execute an "on_exit" command.
     */
    protected void onExit() {
        execute("on_exit");
        out.println("----------\n");
        if(out != System.out) {
            out.close();
        }
    }

    /**
     * Execute the given command.
     *
     *  @param args	command args, args[0] contains name of cmd.
     */
    public String execute(String[] args) {
        return execute(args, true);
    }

    /**
     * Execute the given command.
     *
     *  @param args	command args, args[0] contains name of cmd.
     */
    private String execute(String[] args, boolean first) {
        String response = "";

        CommandInterface ci;

        if(args.length > 0) {

            ci = (CommandInterface) commands.get(args[0]);
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
     * @param message 	the string to be parsed.
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
        return words.toArray(new String[0]);
    }

    // inherited from thread.
    public void run() {
        setDaemon(true);
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

    private static Pattern editPattern =
            Pattern.compile("\\^(.+?)\\^(.*?)\\^?");

    private static Pattern bbPattern = Pattern.compile("(!!)");

    /**
     * Gets the input line. Deals with history. Currently we support
     * simple csh-like history. !! - execute last command, !-3 execute
     * 3 from last command, !2 execute second command in history list,
     * !foo - find last command that started with foo and execute it.
     * Also allows editing of the last command wich ^old^new^ type 
     * replacesments
     *
     * @return the next history line or null if done
     */
    private String getInputLine() throws IOException {
        if (consoleReader != null) {
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

            while ((inputLine = br.readLine()) != null) {
                final String currLine = inputLine;
                Callable<Void> cmd = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        String response =
                                CommandInterpreter.this.execute(currLine);
                        if (!response.equals("OK")) {
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
        } catch (IOException ioe) {
            return false;
        } catch (InterruptedException ex) {
            logger.info("Parallel Load did not shut down properly");
            return false;
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException ex) {
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
        if (consoleReader != null) {
            consoleReader.setDefaultPrompt(prompt);
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
         * Finds the most recent message that starts with
         * the given string
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

    private class CommandGroup implements Iterable<String> {

        private String groupName;

        private String description;

        private Set<String> commands;

        public CommandGroup(String groupName, String description, boolean keepSorted) {
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
