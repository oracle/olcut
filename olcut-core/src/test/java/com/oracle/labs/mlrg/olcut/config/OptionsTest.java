package com.oracle.labs.mlrg.olcut.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OptionsTest {

    public static void main(String[] args) throws IOException {

        TestOptions options = new TestOptions();

        ConfigurationManager cm;
        try {
            cm = new ConfigurationManager(args,options);
        } catch (UsageException e) {
            System.out.println(e.getUsage());
            return;
        }

        String[] unparsedArgs = cm.getUnnamedArguments();

        System.out.println("Unparsed arguments:");
        for (String s : unparsedArgs) {
            System.out.println(s);
        }

        System.out.println(options.toString());

        String arg = "-c /path/to/config.xml,/path/to/otherconfig.xml --trainer trainername --@trainername.epochs 5";
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
    public Random rng = new Random(1);

    @Option(charName='a', longName="output-string", usage="String to output")
    public String outputString = "";

    @Option(charName='e', longName="enum", usage="Enum input")
    public OptionsEnum optEnum;

    @Override
    public String toString() {
        return "rngDraw="+rng.nextInt()+",outputString="+outputString+",deepOptions("+deepOptions.toString()+")";
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

enum OptionsEnum {
    THINGS, STUFF, OTHER_THINGS;
}

