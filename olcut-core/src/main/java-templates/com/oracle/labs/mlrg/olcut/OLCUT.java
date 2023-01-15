/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut;

/**
 * This class stores the current OLCUT version, along with other compile time information.
 */
public final class OLCUT {
    /**
     * OLCUT version string.
     */
    public static final String VERSION = "${project.version}";

    /**
     * OLCUT build timestamp.
     */
    public static final String BUILD_TIMESTAMP = "${maven.build.timestamp}";

    /**
     * OLCUT major version number.
     */
    public static final int MAJOR_VERSION;

    /**
     * OLCUT minor version number.
     */
    public static final int MINOR_VERSION;

    /**
     * OLCUT point release number.
     */
    public static final int POINT_VERSION;

    /**
     * OLCUT version tag.
     */
    public static final String TAG_VERSION;

    /**
     * Is this OLCUT build a unreleased snapshot?
     */
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