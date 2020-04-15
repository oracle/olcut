package com.oracle.labs.mlrg.olcut.extras.completion;

import org.objectweb.asm.tree.ClassNode;

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
        return String.format("%c%s%c%s%c%s%c%s", 30, writtenClassName, 31,  shortName.map(c -> "-" + c).orElse("" + ((char) 0)), 31, "--" + longName, 31, "\"" + usage + "\"");
    }
}
