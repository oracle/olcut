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

package com.oracle.labs.mlrg.olcut.extras.completion;

import java.util.Optional;

public class OptionCompletion {

    public static final String OPTIONS_CLASS = "com/oracle/labs/mlrg/olcut/config/Options";
    public static final String OPTION_CLASS = "Lcom/oracle/labs/mlrg/olcut/config/Option;";

    private Optional<Character> shortName;
    private String longName;
    private String usage;

    public OptionCompletion(Character shortName, String longName, String usage) {
        this.shortName = Optional.ofNullable(shortName);
        this.longName = longName;
        this.usage = usage;
    }

    public OptionCompletion(String longName, String usage) {
        this(null, longName, usage);
    }

    public String completionString(String writtenClassName) {
        return String.format("%c%s%c%s%c%s%c%s", 30, writtenClassName, 31,  shortName.map(c -> "-" + c).orElse(" "), 31, "--" + longName, 31, "\"" + usage + "\"");
    }
}
