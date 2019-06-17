package com.oracle.labs.mlrg.olcut.config.io;

/**
 *
 */
public class ConfigLoaderException extends RuntimeException {

    public ConfigLoaderException(Exception e) {
        super(e);
    }

    public ConfigLoaderException(Exception e, String msg) {
        super(msg,e);
    }

    public ConfigLoaderException(String msg) {
        super(msg);
    }
}
