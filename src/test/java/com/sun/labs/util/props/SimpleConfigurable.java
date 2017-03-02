package com.sun.labs.util.props;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple configurable class with a single configurable component.
 */
public class SimpleConfigurable implements Configurable {
    
    @ConfigInteger(defaultValue=1)
    public static final String PROP_SIMPLE = "simple";
    
    int simple;
    
    Logger logger;
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        simple = ps.getInt(PROP_SIMPLE);
    }
    
    public Level getLogLevel() {
        return logger.getLevel();
    }
}
