package com.oracle.labs.mlrg.olcut.config.io;

/**
 * Thrown by the configuration writing system when something is misconfigured.
 */
public class ConfigWriterException extends RuntimeException {

    public ConfigWriterException(Exception e) {
        super(e);
    }

}
