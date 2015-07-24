package com.sun.labs.util.command;

/**
 * A marker interface for any class that provides @Command commands for a
 * command interpreter.  Also provides a little meta-data.
 */
public interface CommandGroup {
    /**
     * Gets the name of the group of commands contained in this container
     * @return 
     */
    public String getName();
    
    /**
     * Gets a description of this group of commands
     * @return 
     */
    public String getDescription();
}
