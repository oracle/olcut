package com.oracle.labs.mlrg.olcut.util;

/**
 * A listener for receiving events from a {@link SubprocessConnection}.
 */
public interface SubprocessConnectionListener {
    /**
     * Called whenever the subprocess has finished starting. Note that
     * if the {@link SubprocessConnection} is set to startOnLoad, you will
     * not receive this event since the process will have started
     * before the constructor returns.
     */
    public void subprocessStarted(SubprocessConnection connection);

    /**
     * Called immediately before the subprocess will be shut down. Like
     * a shutdown hook for the JVM, this should not be a long-running
     * method.
     */
    public void subprocessPreShutdown(SubprocessConnection connection);

    /**
     * Called immediately after a subprocess has completed shutdown.
     */
    public void subprocessPostShutdown(SubprocessConnection connection);
}
