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

package com.oracle.labs.mlrg.olcut.test.config;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.util.EnumSet;
import java.util.Objects;

/**
 *
 */
public class EnumConfigurable implements Configurable {

    public enum Type { A, B, C, D, E, F};

    @Config
    public Type enum1;

    @Config
    public Type enum2 = Type.A;

    @Config
    public EnumSet<Type> enumSet1 = EnumSet.of(Type.A,Type.F);

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final EnumConfigurable other = (EnumConfigurable) obj;
        if(this.enum1 != other.enum1) {
            return false;
        }
        if(this.enum2 != other.enum2) {
            return false;
        }
        return Objects.equals(this.enumSet1, other.enumSet1);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.enum1 != null ? this.enum1.hashCode() : 0);
        hash = 89 * hash + (this.enum2 != null ? this.enum2.hashCode() : 0);
        hash = 89 * hash + (this.enumSet1 != null ? this.enumSet1.hashCode() : 0);
        return hash;
    }

}
