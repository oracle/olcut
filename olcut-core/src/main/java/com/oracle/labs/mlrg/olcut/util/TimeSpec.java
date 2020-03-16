/*
 * Copyright 1999-2002 Carnegie Mellon University.
 * Portions Copyright 2002 Sun Microsystems, Inc.
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
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

package com.oracle.labs.mlrg.olcut.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to parse time specs like 1d2h30m2s455ms into the number of milliseconds
 * that they represent.  Only integer times will be taken into account.  This will
 * be parsed using a regular expression, so things like 1d300m are allowed and will be
 * parsed correctly.
 */
public class TimeSpec {

    private static Pattern timepat = Pattern.compile("(?:(\\d+)(d))??(?:(\\d+)(h))??(?:(\\d+)(m))??(?:(\\d+)(s))??(?:(\\d+)(ms))??");

    private final static Map<String,TimeUnit> tum;

    static {
        tum = new HashMap<>();
        tum.put("d", TimeUnit.DAYS);
        tum.put("h", TimeUnit.HOURS);
        tum.put("m", TimeUnit.MINUTES);
        tum.put("s", TimeUnit.SECONDS);
        tum.put("ms", TimeUnit.MILLISECONDS);
    }

    public static long parse(String timespec) throws IllegalArgumentException {
        Matcher m = timepat.matcher(timespec);
        long nms = 0;

        if(m.matches()) {
            for(int i = 1; i <= m.groupCount(); i += 2) {
                if(m.group(i) != null) {
                    long dur = Integer.parseInt(m.group(i));
                    TimeUnit tu = tum.get(m.group(i+1));
                    nms += tu.toMillis(dur);
                }
            }
        } else {
            throw new IllegalArgumentException("Couldn't parse timespec " + timespec);
        }
        return nms;
    }
}
