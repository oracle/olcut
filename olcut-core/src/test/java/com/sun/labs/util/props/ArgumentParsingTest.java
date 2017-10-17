package com.sun.labs.util.props;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArgumentParsingTest {

    @Test
    public void testStringSplitting() {
        String a = "nasty,input,string\\,with spaces,\"quoted,commas\",and\\,other things.";
        List<String> expectedOutput = Arrays.asList("nasty","input","string,with spaces","\"quoted,commas\"","and,other things.");

        List<String> output = ConfigurationManager.parseStringList(a);
        assertEquals("String parsing failed",expectedOutput,output);
    }

    @Test
    public void testConfigLoading() throws IOException {
        String[] args = new String[]{"-c","stringListConfig.xml","--other-arguments","that-get-in-the-way"};
        loadFromArgs("-c",args);
        args = new String[]{"--config-file","stringListConfig.xml","--other-arguments","that-get-in-the-way"};
        loadFromArgs("--config-file",args);
        args = new String[]{"-o","--config-file","stringListConfig.xml,componentListConfig.xml","-s"};
        loadFromArgs("--config-file with multiple files",args);
        args = new String[]{"-o","--config-file","componentListConfig.xml","-s","-c","stringListConfig.xml"};
        loadFromArgs("overriding --config-file with -c",args);
    }

    public void loadFromArgs(String name, String[] args) throws IOException {
        ParsingOptions o = new ParsingOptions();
        ConfigurationManager cm = new ConfigurationManager(args,o);
        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        assertEquals("Loading from " + name + " failed.", "a", slc.strings.get(0));
        assertEquals("Loading from " + name + " failed.", "b", slc.strings.get(1));
        assertEquals("Loading from " + name + " failed.", "c", slc.strings.get(2));
    }

    @Test
    public void testConfigurableOverride() throws IOException {
        String[] args = new String[]{"-c","stringListConfig.xml","--@listTest.strings","alpha,beta,gamma"};
        ConfigurationManager cm = new ConfigurationManager(args);

        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        assertEquals("Configurable overriding failed.", "alpha", slc.strings.get(0));
        assertEquals("Configurable overriding failed.", "beta", slc.strings.get(1));
        assertEquals("Configurable overriding failed.", "gamma", slc.strings.get(2));
    }

    @Test
    public void testGlobalOverride() throws IOException {
        String[] args = new String[]{"-c","globalPropertyConfig.xml","--@a","apollo","--@monkeys","gibbons"};
        ConfigurationManager cm = new ConfigurationManager(args);

        assertEquals("Global property overriding failed.","apollo",cm.getGlobalProperty("a"));
        assertEquals("Global property overriding failed.","gibbons",cm.getGlobalProperty("monkeys"));
        assertEquals("Global property overriding failed.","beta",cm.getGlobalProperty("b"));
    }

    @Test(expected=ArgumentException.class)
    public void testConfigurableOverrideFail() throws IOException {
        String[] args = new String[]{"--@listTest.strings","alpha,beta,gamma"};
        ConfigurationManager cm = new ConfigurationManager(args);
        Assert.fail();
    }

    @Test(expected=ArgumentException.class)
    public void testNoConfigParam() throws IOException {
        String[] args = new String[]{"-c"};
        ConfigurationManager cm = new ConfigurationManager(args);
        Assert.fail();
    }

    @Test(expected=ArgumentException.class)
    public void testNoConfigParamStr() throws IOException {
        String[] args = new String[]{"--config-file"};
        ConfigurationManager cm = new ConfigurationManager(args);
        Assert.fail();
    }

    @Test(expected=ArgumentException.class)
    public void testInvalidConfigParam() throws IOException {
        String[] args = new String[]{"-c","monkeys"};
        ConfigurationManager cm = new ConfigurationManager(args);
        Assert.fail();
    }

    @Test(expected=ArgumentException.class)
    public void testDuplicateCharArguments() throws IOException {
        String[] args = new String[]{"-c","stringListConfig.xml"};
        Options o = new DuplicateCharOptions();
        ConfigurationManager cm = new ConfigurationManager(args,o);
        Assert.fail();
    }

    @Test(expected=ArgumentException.class)
    public void testDuplicateLongArguments() throws IOException {
        String[] args = new String[]{"-c","stringListConfig.xml"};
        Options o = new DuplicateLongOptions();
        ConfigurationManager cm = new ConfigurationManager(args,o);
        Assert.fail();
    }

}

class DuplicateCharOptions implements Options {
    @Option(charName = 'd', longName="dvorak", usage="test hard")
    public String dvorak;

    @Option(charName = 'd', longName="diplodocus", usage="test hard 2: test harder")
    public String diplodocus;
}

class DuplicateLongOptions implements Options {
    @Option(charName = 'v', longName="dvorak", usage="test hard")
    public String dvorak;

    @Option(charName = 'd', longName="dvorak", usage="test hard 2: test harder")
    public String dvorak2;
}

class ParsingOptions implements Options {
    @Option(charName = 'o', longName="other", usage="test hard")
    public boolean other;

    @Option(charName = 's', longName="surrounding", usage="test hard 2: test harder")
    public boolean surrounding;

    @Option(longName="other-arguments", usage="test hard with a vengeance")
    public String otherArguments;
}
