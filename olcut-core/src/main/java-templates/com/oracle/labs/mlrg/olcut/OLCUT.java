/*
 * Copyright 2015-2020 Oracle Corporation.
 */
package com.oracle.labs.mlrg.olcut;

/**
 * This class stores the current OLCUT version, along with other compile time information.
 */
public final class OLCUT {
    public static final String VERSION = "${project.version}";

    public static final String BUILD_TIMESTAMP = "${maven.build.timestamp}";

    public static final int MAJOR_VERSION;
    public static final int MINOR_VERSION;
    public static final int POINT_VERSION;
    public static final String TAG_VERSION;

    public static final boolean IS_SNAPSHOT;

    static {
        String[] splitVersion = VERSION.split("\\.");
        MAJOR_VERSION = Integer.parseInt(splitVersion[0]);
        MINOR_VERSION = Integer.parseInt(splitVersion[1]);
        IS_SNAPSHOT = VERSION.contains("SNAPSHOT");
        String[] tags = splitVersion[2].split("-");
        POINT_VERSION = Integer.parseInt(tags[0]);
        if (tags.length > 1) {
            TAG_VERSION = tags[1];
        } else {
            TAG_VERSION = "";
        }
    }
}