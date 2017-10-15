package com.sun.labs.util.props;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class OptionsTest {

    public static void main(String[] args) throws IOException {

        TestOptions options = new TestOptions();

        ConfigurationManager cm;
        try {
            cm = new ConfigurationManager(args, options);
        } catch (UsageException e) {
            System.out.println(e.getMsg());
            return;
        }

        String[] unparsedArgs = cm.getUnnamedArguments();

        System.out.println("Unparsed arguments:");
        for (String s : unparsedArgs) {
            System.out.println(s);
        }

        String arg = "-c /path/to/config.xml,/path/to/otherconfig.xml --trainer trainername --@trainername.epochs 5";
    }

}

class TestOptions implements Options {
    FileOptions things;
    OtherOptions stuff;

    @Option(charName='p', longName="pi", usage="Changes the value of pi. Warning, may break mathematics.")
    public double pi = Math.PI;

    public String getName() { return "TestOptions"; }
}

class FileOptions implements Options {
    @Option(charName='i', longName="input-file", usage="Path to input file")
    public File inputFile;

    @Option(charName='o', longName="output-file", usage="Path to output file")
    public Path outputFile;

    public String getName() {
        return "File Options";
    }
}

class OtherOptions implements Options {
    @Option(charName='r', longName="seed", usage="Random seed")
    public Random rng;

    @Option(charName='a', longName="output-string", usage="String to output")
    public String outputString;

    public String getName() {
        return "Other Options";
    }
}
