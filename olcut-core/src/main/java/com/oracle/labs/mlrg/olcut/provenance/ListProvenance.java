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

package com.oracle.labs.mlrg.olcut.provenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Provenance for a list of provenance objects.
 */
public final class ListProvenance<T extends Provenance> implements Provenance, Iterable<T> {
    private static final long serialVersionUID = 1L;

    private final List<T> list;

    /**
     * Creates a ListProvenance from the supplied list. The
     * list is defensively copied and immutable.
     * @param list The input list.
     */
    public ListProvenance(List<T> list) {
        this.list = List.copyOf(list);
    }

    /**
     * Creates an empty list provenance of the appropriate type.
     */
    public ListProvenance() {
        this.list = Collections.emptyList();
    }

    /**
     * An unmodifiable view on the provenance list.
     * @return The provenance list.
     */
    public List<T> getList() {
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListProvenance)) return false;
        ListProvenance<?> that = (ListProvenance<?>) o;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    /**
     * Creates a list provenance from a {@link Collection} of objects which
     * implement {@link Provenancable}.
     * @param collection The collection to extract provenance from.
     * @param <T> The type of the provenance.
     * @param <U> The provenancable type of the collection.
     * @return A ListProvenance containing the provenances from the collection.
     */
    public static <T extends Provenance, U extends Provenancable<T>> ListProvenance<T> createListProvenance(Collection<U> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ListProvenance<>();
        } else {
            List<T> outputList = new ArrayList<>();

            for (U e : collection) {
                outputList.add(e.getProvenance());
            }

            return new ListProvenance<>(outputList);
        }
    }
}
