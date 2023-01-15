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

/**
 *
 */
public class StringleConfigurable extends StringConfigurable {

    @Config
    public String four = "";

    @Config
    public String five = "";

    public StringleConfigurable() {}

    public StringleConfigurable(String one, String two, String three, String four, String five) {
        super(one,two,three);
        this.four = four;
        this.five = five;
    }

    @Override
    public String toString() {
        return "StringleConfigurable{" + "one=" + one + ", two=" + two + ", three=" + three + ", four=" + four + ", five=" +five + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StringleConfigurable) {
            StringleConfigurable sc = (StringleConfigurable) other;
            return one.equals(sc.one) && two.equals(sc.two) && three.equals(sc.three) && four.equals(sc.four) && five.equals(sc.five);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = one.hashCode();
        result = 31 * result + two.hashCode();
        result = 31 * result + three.hashCode();
        result = 31 * result + four.hashCode();
        result = 31 * result + five.hashCode();
        return result;
    }
}
