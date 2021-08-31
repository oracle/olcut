package com.oracle.labs.mlrg.olcut.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jdk.jfr.internal.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Tests for the standard commands in the CommandInterpreter.
 * As unit tests have not existed for the shell, tests should be added
 * here as any part of the shell is touched. Just add a new nested class
 * for each command.
 */
public class StandardCommandTest {
    public static final Logger logger = Logger.getLogger(StandardCommandTest.class.getName());

    private CommandInterpreter ci;
    private CommandInterpreter.StandardCommands cmds;
    private Handler rootHandler;

    @BeforeAll
    public static void setup() {

    }

    @BeforeEach
    public  void setupEach() {
        //
        // Start with a fresh command interpreter and bury and output
        ci = new CommandInterpreter(false);
        ci.setOutput(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));
        cmds = ci.new StandardCommands();
    }

    @Nested
    @DisplayName("Shell LogLevel Tests")
    class LogLevelTests {
        @BeforeEach
        public void cleanLoggers() throws IOException {
            // Outer class's BeforeEach runs before this one
            LogManager logManager = LogManager.getLogManager();
            logManager.readConfiguration();
            rootHandler = Logger.getLogger("").getHandlers()[0];

            LogRecord rec = new LogRecord(Level.INFO, "Test");
            assertTrue(logger.isLoggable(rec.getLevel()) && rootHandler.isLoggable(rec), "Initial test state incorrect");
            rec.setLevel(Level.FINE);
            assertTrue(!logger.isLoggable(rec.getLevel()) && !rootHandler.isLoggable(rec), "Initial test state incorrect");
        }

        @Test
        public void testLogLevelSpecificClass() {
            LogRecord rec = new LogRecord(Level.FINE, "Test");
            //
            // Move the log level for this class
            cmds.setLogLevel(ci, Level.FINE.getName(), StandardCommandTest.class.getName());
            assertTrue(logger.isLoggable(rec.getLevel()), "FINE should be loggable in logger");
            assertTrue(rootHandler.isLoggable(rec), "Failed to adjust level to FINE in handler");

            //
            // Move the log level back up. This should only move the logger and not the handler
            cmds.setLogLevel(ci, Level.INFO.getName(), StandardCommandTest.class.getName());
            assertTrue(rootHandler.isLoggable(rec), "Root handler should not have moved back to INFO");
            assertFalse(logger.isLoggable(rec.getLevel()), "FINE should no longer be loggable in the logger");
        }

        @Test
        public void testLogLevelNamespace() {
            LogRecord rec = new LogRecord(Level.FINE, "Test");
            //
            // Move the log level for this class
            cmds.setLogLevel(ci, Level.FINE.getName(), "com.oracle.labs.mlrg");
            assertTrue(logger.isLoggable(rec.getLevel()), "Logger should be able to log FINE now");
            assertTrue(rootHandler.isLoggable(rec), "Handlers should be able to log fine");

            //
            // Move the log level back up. This should only move the logger and not the handler
            cmds.setLogLevel(ci, Level.INFO.getName(), "com.oracle.labs.mlrg");
            assertTrue(rootHandler.isLoggable(rec), "Root handler should not have moved back to INFO");
            assertFalse(logger.isLoggable(rec.getLevel()), "FINE should no longer be loggable in the logger");
        }

        @Test
        public void testLogLevelHandler() {
            LogRecord rec = new LogRecord(Level.FINE, "Test");

            cmds.setLogLevel(ci, Level.FINER.getName(), "");
            assertTrue(rootHandler.isLoggable(rec), "FINE should now be loggable in the handler");
        }
    }
}
