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

package com.oracle.labs.mlrg.olcut.config;

import java.util.List;

/**
 *
 */
public class ListConfig implements Configurable {

    @Config(mandatory=false)
    public List<String> stringList;

    @Config(mandatory=true)
    public List<Double> doubleList;

    @Config(mandatory=false)
    public List<StringConfigurable> stringConfigurableList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListConfig that = (ListConfig) o;

        if (stringList != null ? !stringList.equals(that.stringList) : that.stringList != null) return false;
        if (doubleList != null ? !doubleList.equals(that.doubleList) : that.doubleList != null) return false;
        return stringConfigurableList != null ? stringConfigurableList.equals(that.stringConfigurableList) : that.stringConfigurableList == null;
    }

    @Override
    public int hashCode() {
        int result = stringList != null ? stringList.hashCode() : 0;
        result = 31 * result + (doubleList != null ? doubleList.hashCode() : 0);
        result = 31 * result + (stringConfigurableList != null ? stringConfigurableList.hashCode() : 0);
        return result;
    }
}
