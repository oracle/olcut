/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A marshalled provenance which contains a list of other {@link FlatMarshalledProvenance} objects.
 * This can recursively include other lists or maps.
 */
public final class ListMarshalledProvenance implements FlatMarshalledProvenance, Iterable<FlatMarshalledProvenance> {
    private static final Logger logger = Logger.getLogger(ListMarshalledProvenance.class.getName());

    private final List<FlatMarshalledProvenance> list;

    /**
     * Constructs a ListMarshalledProvenance wrapped around the supplied list.
     * @param list The list.
     */
    public ListMarshalledProvenance(List<FlatMarshalledProvenance> list) {
        this.list = Collections.unmodifiableList(list);
    }

    /**
     * Gets an unmodifiable view on the marshalled provenance list.
     * @return The marshalled provenance list.
     */
    public List<FlatMarshalledProvenance> getList() {
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListMarshalledProvenance)) return false;
        ListMarshalledProvenance that = (ListMarshalledProvenance) o;
        return getList().equals(that.getList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getList());
    }

    @Override
    public String toString() {
        return "ListMarshalledProvenance" + list.toString();
    }

    @Override
    public Iterator<FlatMarshalledProvenance> iterator() {
        return list.iterator();
    }
}
