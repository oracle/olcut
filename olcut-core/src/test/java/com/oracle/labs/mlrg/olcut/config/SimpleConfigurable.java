package com.oracle.labs.mlrg.olcut.config;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple configurable class with a single configurable component.
 */
public class SimpleConfigurable implements Configurable {
    private static final Logger logger = Logger.getLogger(SimpleConfigurable.class.getName());

    @Config
    int simple = 1;

    public Level getLogLevel() {
        return logger.getLevel();
    }
}
