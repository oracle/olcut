package com.oracle.labs.mlrg.olcut.config;

/**
 * An exception which denotes the usage statement was requested.
 */
public class UsageException extends ArgumentException {
    public UsageException(String msg) {
        super("--usage", msg);
    }

    public String getUsage() {
        return msg;
    }
}
