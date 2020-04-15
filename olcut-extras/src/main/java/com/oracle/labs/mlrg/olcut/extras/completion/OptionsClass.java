package com.oracle.labs.mlrg.olcut.extras.completion;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OptionsClass {
    private static final Logger logger = Logger.getLogger(OptionsClass.class.getName());

    private List<OptionCompletion> localCompletions;
    private ClassNode clazz;
    private List<OptionsClass> parents;

    private boolean cachedCompletions = false;
    private List<OptionCompletion> allCompletions;

    public OptionsClass(ClassNode clazz) {
        this.clazz = clazz;
        this.localCompletions = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.allCompletions = new ArrayList<>();
        defaultCompletions(this.allCompletions);
    }

    // There are some completions that come by default with a configuration manager that we should also support
    private void defaultCompletions(List<OptionCompletion> comps) {
        // TODO It would probably be better to get these directly from ConfigurationManager, but this we can avoid depending on olcut-core
        comps.add(new OptionCompletion('c', "config-file", "A comma separated list of olcut config files."));
        comps.add(new OptionCompletion("config-file-formats", "A comma separated list of olcut FileFormatFactory implementations (assumed to be on the classpath)."));
        comps.add(new OptionCompletion("usage" , "Write out this usage/help statement."));
    }

    public void addCompletion(OptionCompletion completion) {
        localCompletions.add(completion);
    }

    public List<OptionCompletion> getCompletions() {
        if(!cachedCompletions) {
            allCompletions.addAll(localCompletions);
            allCompletions.addAll(parents.stream()
                    .flatMap(p -> p.getCompletions().stream())
                    .collect(Collectors.toList()));
            cachedCompletions = true;
        }
        return allCompletions;
    }

    public void findParents(Map<String, OptionsClass> optionsMap) {
        for(FieldNode field: clazz.fields) {
            String trimmed = GenCompletion.trimDesc(field.desc);
            if (optionsMap.containsKey(trimmed)) {
                parents.add(optionsMap.get(trimmed));
            }
        }
    }

    @Override
    public String toString() {
        return "OptionsClass(name=" + clazz.name + ", parents=" + parents.toString() + ", cached=" + cachedCompletions + ")";
    }
}
