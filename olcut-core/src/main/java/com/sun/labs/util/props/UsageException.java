package com.sun.labs.util.props;

/**
 * An exception which denotes the usage statement was requested.
 */
public class UsageException extends ArgumentException {
    public UsageException(String msg) {
        super("--usage", msg);
    }
}
