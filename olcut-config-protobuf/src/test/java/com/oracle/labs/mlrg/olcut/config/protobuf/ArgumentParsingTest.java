/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.protobuf;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Option;
import com.oracle.labs.mlrg.olcut.config.Options;
import com.oracle.labs.mlrg.olcut.config.test.StringListConfigurable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArgumentParsingTest {

    @Test
    public void testConfigLoading() throws IOException {
        String[] args = new String[]{"-c","stringListConfig.pb","--other-arguments","that-get-in-the-way", "--config-file-formats", "com.oracle.labs.mlrg.olcut.config.protobuf.ProtoConfigFactory"};
        loadFromArgs("-c",args);
        args = new String[]{"--config-file-formats", "com.oracle.labs.mlrg.olcut.config.protobuf.ProtoConfigFactory", "--config-file","stringListConfig.pb","--other-arguments","that-get-in-the-way"};
        loadFromArgs("--config-file",args);
        args = new String[]{"-o","--config-file","stringListConfig.pb,componentListConfig.pbtxt", "--config-file-formats", "com.oracle.labs.mlrg.olcut.config.protobuf.ProtoConfigFactory,com.oracle.labs.mlrg.olcut.config.protobuf.ProtoTxtConfigFactory","-s"};
        loadFromArgs("--config-file with multiple files",args);
        args = new String[]{"-o","--config-file","componentListConfig.pbtxt","-s", "--config-file-formats", "com.oracle.labs.mlrg.olcut.config.protobuf.ProtoTxtConfigFactory,com.oracle.labs.mlrg.olcut.config.protobuf.ProtoConfigFactory","-c","stringListConfig.pb"};
        loadFromArgs("overriding --config-file with -c",args);
    }

    public void loadFromArgs(String name, String[] args) throws IOException {
        ParsingOptions o = new ParsingOptions();
        ConfigurationManager cm = new ConfigurationManager(args,o);
        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        assertEquals("a", slc.strings.get(0), "Loading from " + name + " failed.");
        assertEquals("b", slc.strings.get(1), "Loading from " + name + " failed.");
        assertEquals("c", slc.strings.get(2), "Loading from " + name + " failed.");
    }

    public static class ParsingOptions implements Options {
        @Option(charName = 'o', longName="other", usage="test hard")
        public boolean other;

        @Option(charName = 's', longName="surrounding", usage="test hard 2: test harder")
        public boolean surrounding;

        @Option(longName="other-arguments", usage="test hard with a vengeance")
        public String otherArguments;
    }
}

