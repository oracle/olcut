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
    }

    // There are some completions that come by default with a configuration manager that we should also support
    private List<OptionCompletion> defaultCompletions() {
        // TODO It would probably be better to get these directly from ConfigurationManager, but this we can avoid depending on olcut-core
        List<OptionCompletion> comps = new ArrayList<>();
        comps.add(new OptionCompletion('c', "config-file", "A comma separated list of olcut config files."));
        comps.add(new OptionCompletion("config-file-formats", "A comma separated list of olcut FileFormatFactory implementations (assumed to be on the classpath)."));
        comps.add(new OptionCompletion("usage" , "Write out this usage/help statement."));
        return comps;
    }

    public void addCompletion(OptionCompletion completion) {
        localCompletions.add(completion);
    }

    public List<OptionCompletion> getCompletions() {
        List<OptionCompletion> comps = innerGetCompletions();
        // TODO there is some nonsense on Configuration manager that can turn of -c and --config-file-formats, not currently handled
        comps.addAll(defaultCompletions());
        return comps;
    }

    private List<OptionCompletion> innerGetCompletions() {
        if(!cachedCompletions) {
            allCompletions.addAll(localCompletions);
            allCompletions.addAll(parents.stream()
                    .flatMap(p -> p.innerGetCompletions().stream())
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
