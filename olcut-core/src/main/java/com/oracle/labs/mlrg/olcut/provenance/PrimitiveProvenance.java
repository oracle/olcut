/*
 * Copyright (c) 2019, 2022, Oracle and/or its affiliates.
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

import com.oracle.labs.mlrg.olcut.provenance.primitives.BooleanProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ByteProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.CharProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DateProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DateTimeProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.EnumProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FileProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FloatProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.HashProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.LongProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ShortProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.TimeProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.URLProvenance;

/**
 * A supertype for {@link Provenance}s which are well understood
 * by the provenance system, and are expressible as a single {@link String}.
 * These classes form a sealed set and live in {@link com.oracle.labs.mlrg.olcut.provenance.primitives}.
 * <p>
 * When adding a new PrimitiveProvenance the {@link ProvenanceUtil} and {@link com.oracle.labs.mlrg.olcut.provenance.io.SimpleMarshalledProvenance}
 * classes must also be updated to be taught about the new type.
 */
public sealed interface PrimitiveProvenance<T> extends Provenance permits BooleanProvenance, ByteProvenance,
        CharProvenance, DateProvenance, DateTimeProvenance, DoubleProvenance, EnumProvenance, FileProvenance,
        FloatProvenance, HashProvenance, IntProvenance, LongProvenance, ShortProvenance, StringProvenance,
        TimeProvenance, URLProvenance {

    /**
     * Gets the key associated with this provenance,
     * which is usually the field name associated with its host
     * object.
     * @return The key.
     */
    default public String getKey() {
        return key();
    }

    /**
     * Gets the key associated with this provenance,
     * which is usually the field name associated with its host
     * object.
     * @return The key.
     */
    public String key();

    /**
     * Gets the value of this provenance.
     * @return The value.
     */
    default public T getValue() {
        return value();
    }

    /**
     * Gets the value of this provenance.
     *
     * @return The value.
     */
    public T value();

}
