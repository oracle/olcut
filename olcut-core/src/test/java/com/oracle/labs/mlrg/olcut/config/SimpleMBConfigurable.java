/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SimpleMBConfigurable implements Configurable, ConfigurableMXBean {
    @Config
    private int a = 1;

    @Config
    private String b = "hello";

    @Config
    List<String> c = Arrays.asList("foo","bar");

    public String[] getProperties() {
        return new String[] {"a", "b", "c"};
    }

    public String getValue(String property) {
        if(property.equals("a")) {
            return String.valueOf(a);
        } else if(property.equals("b")) {
            return b;
        } else {
            return null;
        }
    }

    public String[] getValues(String property) {
        if(property.equals("c")) {
            return c.toArray(new String[0]);
        }
        return null;
    }

    public boolean setValue(String property, String value) {
        if(property.equals("a")) {
            try {
                a = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        } else if(property.equals("b")) {
            b = value;
            return true;
        } else {
            return false;
        }
    }

    public boolean setValues(String property, String[] values) {
        c = new ArrayList<String>();
        for(String value : values) {
            c.add(value);
        }
        return true;
    }

}
