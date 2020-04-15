package com.oracle.labs.mlrg.olcut.extras.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An object that stores all the state necessary to write out the completion text for a single main class
 */
public class ClassCompletion {

    public static String writeClassName(String cn) {
        return cn.replace('/', '.');
    }

    private String className;
    private List<OptionCompletion> options;

    public ClassCompletion(String className) {
        this(className, new ArrayList<>());
    }

    public ClassCompletion(String className, List<OptionCompletion> options) {
        this.className = className;
        this.options = options;
    }

    public void addOptions(List<OptionCompletion> options) {
        this.options.addAll(options);
    }

    public String completionString() {
        String writtenClassName = writeClassName(className);
        return writtenClassName + (options != null && !options.isEmpty()
                ? "\n" + options.stream()
                .map(o -> o.completionString(writtenClassName))
                .collect(Collectors.joining("\n"))
                : "");
    }
}
