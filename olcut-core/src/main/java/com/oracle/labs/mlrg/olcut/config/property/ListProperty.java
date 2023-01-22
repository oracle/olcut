/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * A ListProperty is a container for two lists, one of {@link SimpleProperty} instances and
 * one of {@link Class} instances. The Class instances are used to look up all instances of that class and
 * insert them into the field.
 */
public record ListProperty(List<SimpleProperty> simpleList, List<Class<?>> classList) implements Property {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ListProperty.class.getName());

    public ListProperty(List<SimpleProperty> simpleList, List<Class<?>> classList) {
        this.simpleList = Collections.unmodifiableList(simpleList);
        this.classList = Collections.unmodifiableList(classList);
    }

    public ListProperty(List<SimpleProperty> simpleList) {
        this(simpleList, Collections.emptyList());
    }

    @Override
    public ListProperty copy() {
        ArrayList<SimpleProperty> newSimpleList = new ArrayList<>();
        for (SimpleProperty p : simpleList) {
            newSimpleList.add(p.copy());
        }
        if (classList.isEmpty()) {
            return new ListProperty(newSimpleList);
        } else {
            return new ListProperty(newSimpleList, new ArrayList<>(classList));
        }
    }

    @Override
    public String toString() {
        return "[" + simpleList.toString() + ", " + classList.toString() + "]";
    }

    public static ListProperty createFromStringList(List<String> stringList) {
        List<SimpleProperty> output = new ArrayList<>();

        for (String s : stringList) {
            output.add(new SimpleProperty(s));
        }

        return new ListProperty(output);
    }
}
