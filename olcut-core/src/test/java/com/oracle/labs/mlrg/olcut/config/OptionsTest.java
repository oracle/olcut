/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates.
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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class OptionsTest {

    @Test
    public void testBasic() {
        String[] args = new String[] {"--deeper-string","How low can you go?",
                "--deep-string", "double bass",
                "--enum", "THINGS", 
                "--output-string", "ringstay putoutay",
                "--seed", "42",
                "--baz", "X,Y,Z",
                "--output-file", "/tmp/output.txt",
                "--input-file", "/tmp/input.txt",
                "--pi", "3.1415927"};
        TestOptions options = new TestOptions();
        ConfigurationManager cm = new ConfigurationManager(args,options);
        
        assertEquals(3.1415927d, options.pi, 0.00001);
        assertEquals("input.txt", options.foo.inputFile.getName());
        assertEquals("output.txt", options.foo.outputFile.getFileName().toString());
        assertEquals(Arrays.asList("X", "Y", "Z"), options.foo.baz);
        assertEquals(-1_170_105_035, new Random(options.bar.seed).nextInt());
        assertEquals(OptionsEnum.THINGS, options.bar.optEnum);
        assertEquals("ringstay putoutay", options.bar.outputString);
        assertEquals("double bass", options.bar.deepOptions.deepString);
        assertEquals("How low can you go?", options.bar.deepOptions.deeperOptions.deeperString);

        String[] unparsedArgs = cm.getUnnamedArguments();
        assertEquals(0, unparsedArgs.length);

    }
    
    @Test
    public void testBackSlash() {
        String deeperStringValue = ConfigurationManager.IS_WINDOWS ? "\\s+" : "\\\\s+";
        String[] args = new String[] {"--deeper-string", deeperStringValue};
        TestOptions options = new TestOptions();
        ConfigurationManager cm = new ConfigurationManager(args,options);
        assertEquals("\\s+", options.bar.deepOptions.deeperOptions.deeperString);
    }

    public static class CommaOptions implements Options {
        @Option(longName="my-chars",usage="The characters.")
        public char[] myChars;
    }

    @Test
    public void testComma() {
        String[] args = new String[] {"--my-chars", "a," + ConfigurationManager.CUR_ESCAPE_CHAR + ",,b,c"};
        CommaOptions options = new CommaOptions();
        ConfigurationManager cm = new ConfigurationManager(args, options);
        assertEquals(4, options.myChars.length);
        assertEquals('a', options.myChars[0]);
        assertEquals(',', options.myChars[1]);
        assertEquals('b', options.myChars[2]);
        assertEquals('c', options.myChars[3]);
    }

    private static class PrivateClassOptions implements Options {
        @Option(charName='s',longName="something",usage="Provide something")
        public String s;
    }

    public static class PrivateConstructorOptions implements Options {
        private PrivateConstructorOptions() {
        }

        @Option(charName='s',longName="something",usage="Provide something")
        public String s;
    }

    public static class NoDefaultConstructorOptions implements Options {
        public NoDefaultConstructorOptions(String aValue) {
        }

        @Option(charName='s',longName="something",usage="Provide something")
        public String s;
    }

    @Test
    public void testAccess() {
        PrivateClassOptions one = new PrivateClassOptions();
        Exception e = assertThrows(ArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ConfigurationManager cm = new ConfigurationManager(new String[]{"-s", "donkey"}, one);
            }
        });
        assertTrue(e.getMessage().contains("must be public"));

        PrivateConstructorOptions two = new PrivateConstructorOptions();
        e = assertThrows(ArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ConfigurationManager cm = new ConfigurationManager(new String[]{"-s", "donkey"}, two);
            }
        });
        assertTrue(e.getMessage().contains("default constructor must be public"));

        NoDefaultConstructorOptions three = new NoDefaultConstructorOptions("donkey");
        e = assertThrows(ArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ConfigurationManager cm = new ConfigurationManager(new String[]{"-s", "donkey"}, three);
            }
        });
        assertTrue(e.getMessage().contains("no default constructor"));

    }

    @Test
    public void enumSetUsage() {
        EnumSetOptions opts = new EnumSetOptions();

        String[] args = new String[]{"--enum-set", "THINGS,STUFF"};

        ConfigurationManager cm = new ConfigurationManager(args, opts);

        EnumSet<OptionsEnum> enumSet = EnumSet.of(OptionsEnum.THINGS, OptionsEnum.STUFF);
        assertEquals(opts.enumSet,enumSet);

        String usage = cm.usage();

        assertTrue(usage.contains("EnumSet - {THINGS, STUFF, OTHER_THINGS}"));
    }
}

class TestOptions implements Options {
    FileOptions foo;
    OtherOptions bar;

    @Override
    public String getOptionsDescription() {
        return "Options for testing things.";
    }

    @Option(charName='p', longName="pi", usage="Changes the value of pi. Warning, may break mathematics.")
    public double pi = Math.PI;

    @Override
    public String toString() {
        return "pi="+pi+",FileOptions("+foo.toString()+"),OtherOptions("+bar.toString()+")";
    }
}

class FileOptions extends IOOptions {
    @Override
    public String getOptionsDescription() {
        return "Options for working with files.";
    }
    @Option(charName='i', longName="input-file", usage="Path to input file")
    public File inputFile = new File(".");

    @Option(charName='o', longName="output-file", usage="Path to output file")
    public Path outputFile = new File(".").toPath();

    @Override
    public String toString() {
        return "inputFile="+inputFile+",outputFile="+outputFile+",IOOptions("+super.toString()+")";
    }
}

class IOOptions implements Options {
    @Option(charName='f', longName="baz", usage="quux")
    public List<String> baz = new ArrayList<>();

    @Override
    public String toString() {
        return "baz="+baz.toString();
    }
}

class OtherOptions implements Options {
    @Override
    public String getOptionsDescription() {
        return "Other options.";
    }
    public DeepOptions deepOptions;

    @Option(charName='r', longName="seed", usage="Random seed")
    public long seed = 1L;

    @Option(charName='a', longName="output-string", usage="String to output")
    public String outputString = "";

    @Option(charName='e', longName="enum", usage="Enum input")
    public OptionsEnum optEnum;

    @Override
    public String toString() {
        return "rngDraw="+new Random(seed)+",outputString="+outputString+",deepOptions("+deepOptions.toString()+")";
    }
}

class DeepOptions implements Options {
    @Override
    public String getOptionsDescription() {
        return "Like options, but deep.";
    }
    public DeeperOptions deeperOptions;

    @Option(charName = 'b', longName = "deep-string", usage = "Deep string.")
    public String deepString;

    @Override
    public String toString() {
        return "deepString="+deepString+",deeperOptions("+deeperOptions.toString()+")";
    }
}

class DeeperOptions implements Options {
    @Override
    public String getOptionsDescription() {
        return "Like deep options, but deeper.";
    }
    @Option(charName='d', longName="deeper-string", usage="Deeper string.")
    public String deeperString;

    @Override
    public String toString() {
        return "deeperString="+deeperString;
    }
}

class EnumSetOptions implements Options {
    @Override
    public String getOptionsDescription() {
        return "It's got an enumset.";
    }
    @Option(charName='e', longName="enum-set", usage="It's an enum set")
    public EnumSet<OptionsEnum> enumSet;

    @Override
    public String toString() {
        return "enumSet="+enumSet.toString();
    }
}

enum OptionsEnum {
    THINGS, STUFF, OTHER_THINGS;
}

