package com.sun.labs.util.service;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * An adapter that has a run method that waits for a latch to be ready, and a
 * stop method that counts the latch down.
 */
public class ConfigurableServiceAdapter implements ConfigurableService {
    public static final Logger logger = Logger.getLogger(ConfigurableServiceAdapter.class.getName());

    protected CountDownLatch done = new CountDownLatch(1);
    
    protected ConfigurableServiceStarter starter;

    @Override
    public String getServiceName() {
        return "ConfigurableServiceAdapter";
    }

    @Override
    public void setStarter(ConfigurableServiceStarter starter) {
        this.starter = starter;
    }

    @Override
    public void run() {
        try {
            done.await();
        } catch (InterruptedException ex) {
            //
            // We'll just return if we get interrupted, since things are weird.
        }
    }
    
    @Override
    public void stop() {
        done.countDown();
    }
}
