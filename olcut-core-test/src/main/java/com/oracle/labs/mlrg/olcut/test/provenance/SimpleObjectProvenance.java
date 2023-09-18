/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.test.provenance;

import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class SimpleObjectProvenance implements ObjectProvenance {
    private final ListProvenance<? extends Provenance> prov;

    public SimpleObjectProvenance(ListProvenance<? extends Provenance> prov) {
        this.prov = prov;
    }

    public SimpleObjectProvenance(Map<String, Provenance> prov) {
        this.prov = (ListProvenance<?>) prov.get("prov");
    }

    @Override
    public String getClassName() {
        return ProvenanceTestUtils.class.getName();
    }

    @Override
    public Iterator<Pair<String, Provenance>> iterator() {
        return Collections.singletonList(new Pair<>("prov", (Provenance) prov)).iterator();
    }

    @Override
    public String toString() {
        return "SimpleObjectProvenance{" +
                "prov=" + prov +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleObjectProvenance)) return false;
        SimpleObjectProvenance pairs = (SimpleObjectProvenance) o;
        return prov.equals(pairs.prov);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prov);
    }
}
