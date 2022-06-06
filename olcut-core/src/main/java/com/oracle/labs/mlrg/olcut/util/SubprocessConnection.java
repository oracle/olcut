package com.oracle.labs.mlrg.olcut.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A connection to a sub-process that can be communicated with over stdio. The subprocess
 * starts implicitly on activity or can be started explicitly. An idle timeout can also
 * be used to shut the process down when not in use. Subprocesses should be implemented to take
 * commands or opcodes and return output followed by an empty line as a response. They should also
 * watch for a "SHUTDOWN" command (the word "SHUTDOWN" on a line by itself) for terminating
 * the subprocess cleanly.
 *
 * Commands invoked by SubprocessConnection are assumed either to be stateless or to persist
 * state themselves. It is safe to call {@link #shutdown} on this class to cause the subprocess
 * to terminate, then to issue more commands, causing the subprocess to be restarted. Setting
 * an idle timeout for the process with {@link #setTimeout} will cause this behavior to happen
 * if the idle timeout is reached and new commands are sent.
 */
public final class SubprocessConnection {
    public static final Logger logger = Logger.getLogger(SubprocessConnection.class.getName());

    public static final String SHUTDOWN = "SHUTDOWN";
    public static final String PYTHONUNBUFFERED = "PYTHONUNBUFFERED";

    private final String command;

    private Map<String,String> environment = new HashMap<>();

    private long timeoutMillis;

    private Process process = null;

    private Timer idlerTimer = null;

    private long lastIOTime;

    private final ArrayList<SubprocessConnectionListener> listeners = new ArrayList<>();

    /**
     * Create a sub-process connection, but does not start the process.
     *
     * NB: In case the subprocess is <code>python</code> in it, SubprocessConnection ensures
     * that <code>$PYTHONUNBUFFERED</code> is set in the environment. Without this setting, python
     * will not automatically flush responses to stdio and the process will appear to be hung.
     *
     * @param command a shell command to invoke that starts the process
     */
    public SubprocessConnection(String command) {
        this.command = command;
        environment.put(PYTHONUNBUFFERED, "True");
    }

    /**
     * Creates a sub-process connection that can start the subprocess immediately without
     * waiting for any interaction with the process.
     *
     * NB: In case the subprocess is <code>python</code> in it, SubprocessConnection ensures
     * that <code>$PYTHONUNBUFFERED</code> is set in the environment. Without this setting, python
     * will not automatically flush responses to stdio and the process will appear to be hung.
     *
     * @param command a shell command to invoke that starts the process
     * @param startImmediately whether the process should start right away
     * @throws IOException if the process can't be used properly
     */
    public SubprocessConnection(String command, boolean startImmediately) throws IOException {
        this(command, startImmediately, null);
    }

    /**
     * Creates a sub-process connection with the provided environment that can be started immediately
     * without waiting for any interaction with the process.
     *
     * NB: In case the subprocess is <code>python</code>, SubprocessConnection always ensures
     * that <code>$PYTHONUNBUFFERED</code> is set in the environment. Without this setting, python
     * will not automatically flush responses to stdio and the process will appear to be hung.
     *
     * @param command a shell command to invoke that starts the process
     * @param startImmediately whether the process should start right away
     * @param environment environment variable values to set for the command
     * @throws IOException if the process can't be used properly
     */
    public SubprocessConnection(String command, boolean startImmediately, Map<String,String> environment)
            throws IOException {
        if (environment != null) {
            this.environment = new HashMap<>(environment);
        }
        this.command = command;
        if (startImmediately) {
            ensureRunning();
        }

        //
        // Ensure PYTHONUNBUFFERED is set, but don't trample a value if there is one
        if (!this.environment.containsKey(PYTHONUNBUFFERED)) {
            this.environment.put(PYTHONUNBUFFERED, "True");
        }
    }

    /**
     * Adds a listener to receive events related to this SubprocessConnection.
     * 
     * @param l the listener to add
     */
    public void addSubprocessListener(SubprocessConnectionListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    /**
     * Removes a listener so that it no longer receives events related to this object.
     * Does nothing if the listener was not added.
     *
     * @param l the listener to remove
     */
    public void removeSubprocessListener(SubprocessConnectionListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    /**
     * Gets the command that was used to start this subprocess
     * @return the subprocess command string
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets a timeout that should be used to shut down the subprocess.  If no
     * timeout is set, no automatic shutdown will occur. If the subprocess is
     * shutdown by the idle timeout, it will be restarted if a new command is run.
     * Idle time is checked every 30 seconds.
     *
     * @param time the length of time before the timeout
     * @param unit the unit of time that the length is specified in
     */
    public void setTimeout(int time, TimeUnit unit) {
        if (time > 0 && unit != null) {
            timeoutMillis = unit.toMillis(time);
        }
    }

    /**
     * Run a command in the subprocess. Sends the command to the subprocess and reads
     * any text that is returned up until an empty line is printed. A string containing
     * the returned text, suitable for being wrapped in a StringReader, is returned.
     *
     * @param command the text to send to the subprocess
     * @return all text returned from the subprocess
     */
    public String run(String command) throws IOException {
        StringBuilder results = new StringBuilder();
        ensureRunning();
        synchronized (process) {
            //
            // Get this process's stdin and write the command string to it
            PrintWriter stdin = new PrintWriter(process.getOutputStream());
            logger.fine("SENT::" + command);
            stdin.println(command);
            stdin.flush();

            //
            // Read until an empty line is returned
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while (!(line = stdout.readLine().trim()).isEmpty()) {
                logger.finer(line);
                //
                // Accumulate answers.
                results.append(line).append(System.lineSeparator());
            }
            lastIOTime = System.currentTimeMillis();
        }
        return results.toString();
    }

    /**
     * Makes sure the subprocess (and if applicable the idler) is running. Can be called before
     * every use of the subprocess.
     *
     * @throws IOException if subprocess startup fails
     */
    private synchronized void ensureRunning() throws IOException {
        //
        // Let's be sure nobody else can mess with process while we're
        // messing with process. The method is synchronized so we only do it
        // one at a time, and we synchronized on process below as well, so we don't
        // trample other parts of the class.
        if (process == null || !process.isAlive()) {
            ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
            logger.info("Running subprocess " + Arrays.toString(command.split("\\s+")));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.environment().putAll(environment);
            process = pb.start();
            synchronized (process) {
                BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = stdout.readLine()) != null) {
                    logger.fine("RECEIVED::" + line);
                    if (line.startsWith("Ready")) {
                        logger.info("Subprocess is ready");
                        break;
                    }
                }
                lastIOTime = System.currentTimeMillis();
            }
            //
            // Notify our listeners that we just started
            listeners.forEach(l -> l.subprocessStarted(this));
        }
        //
        // If the idler isn't running, start it up too. Check if idle time has elapsed every 30 seconds.
        if (timeoutMillis != 0 && idlerTimer == null) {
            idlerTimer = new Timer("SubProcessIdler", true);
            idlerTimer.schedule(new Idler(), 30 * 1000, 30 * 1000);
        }
    }

    /**
     * Shuts down the running subprocess. Attempts to shut down gracefully by issuing the
     * text "SHUTDOWN" on a line by itself to the process. If the process does not exit
     * after 5 seconds, the process is terminated forcibly.
     */
    public void shutdown() {
        if (process != null) {
            synchronized (process) {
                if (process.isAlive()) {
                    //
                    // Announce that we're about to shut down.
                    listeners.forEach(l -> l.subprocessPreShutdown(this));

                    // First, tell it to shut down, then wait a short
                    // period before making sure it is down.
                    PrintWriter stdin = new PrintWriter(process.getOutputStream());
                    stdin.println(SHUTDOWN);
                    stdin.flush();
                    try {
                        if (!process.waitFor(5, TimeUnit.SECONDS)) {
                            process.destroyForcibly();
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Shutdown interrupted", e);
                        process.destroyForcibly();
                    }
                    //
                    // Announce that shutdown has happened.
                    listeners.forEach(l -> l.subprocessPostShutdown(this));
                }
            }
        }
        if (idlerTimer != null) {
            idlerTimer.cancel();
            idlerTimer = null;
        }

    }

    /**
     * A thread that will watch for the subprocess to have become idle and
     * terminate it if the idle timeout period has elapsed.
     */
    public class Idler extends TimerTask {

        @Override
        public void run() {
            if (process != null) {
                synchronized (process) {
                    if (process.isAlive()) {
                        long currTime = System.currentTimeMillis();
                        if (currTime - lastIOTime > timeoutMillis) {
                            //
                            // Time expired, shut down the subprocess.
                            logger.info("Shutting down subprocess due to idle timeout.");
                            shutdown();
                        }
                    }
                }
            }
        }
    }

}