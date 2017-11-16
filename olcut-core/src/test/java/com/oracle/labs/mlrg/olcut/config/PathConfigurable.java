package com.oracle.labs.mlrg.olcut.config;

import java.nio.file.Path;

/**
 *
 */
public class PathConfigurable implements Configurable {

    @Config
    private Path test;

    public Path getPath() {
        return test;
    }

}
