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

package com.oracle.labs.mlrg.olcut.config.test;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.util.logging.Logger;

/**
 * A basic configurable object that uses the Config annotation directly on the
 * types.
 */
public class BasicConfigurable implements Configurable {
    private static final Logger logger = Logger.getLogger(BasicConfigurable.class.getName());
    
    @Config
    public String s = "default";

    @Config
    public int i = 16;
    
    @Config
    public Integer bigI = 17;

    @Config
    public long l = 18;
    
    @Config
    public Long bigL = 19L;
    
    @Config
    public double d = 21;
    
    @Config
    public Double bigD = 22d;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicConfigurable that = (BasicConfigurable) o;

        if (i != that.i) return false;
        if (l != that.l) return false;
        if (Double.compare(that.d, d) != 0) return false;
        if (s != null ? !s.equals(that.s) : that.s != null) return false;
        if (bigI != null ? !bigI.equals(that.bigI) : that.bigI != null) return false;
        if (bigL != null ? !bigL.equals(that.bigL) : that.bigL != null) return false;
        return bigD != null ? bigD.equals(that.bigD) : that.bigD == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = s != null ? s.hashCode() : 0;
        result = 31 * result + i;
        result = 31 * result + (bigI != null ? bigI.hashCode() : 0);
        result = 31 * result + (int) (l ^ (l >>> 32));
        result = 31 * result + (bigL != null ? bigL.hashCode() : 0);
        temp = Double.doubleToLongBits(d);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (bigD != null ? bigD.hashCode() : 0);
        return result;
    }
}
