/*
 * Copyright (c) 2004, 2025, Oracle and/or its affiliates.
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
package com.oracle.labs.mlrg.olcut.util;

import java.io.PrintWriter;
import java.text.Normalizer;

/**
 * String utilities.
 */
public final class StringUtil {

    // Unconstructable.
    private StringUtil() { }

    /**
     * Pads with spaces or truncates the given string to guarantee that it is
     * exactly the desired length.
     *
     * @param string    the string to be padded
     * @param minLength the desired length of the string
     * @return a string of length containing string
     * padded with whitespace or truncated
     */
    public static String pad(String string, int minLength) {
        String result = string;
        int pad = minLength - string.length();
        if (pad > 0) {
            result = string + " ".repeat(minLength - string.length());
        } else if (pad < 0) {
            result = string.substring(0, minLength);
        }
        return result;
    }

    /**
     * Pads with spaces or truncates the given int to guarantee that it is
     * exactly the desired length.
     *
     * @param val       the val to be padded
     * @param minLength the desired length of the string
     * @return a string of length containing string
     * padded with whitespace or truncated
     */
    public static String pad(int val, int minLength) {
        return pad("" + val, minLength);
    }

    /**
     * Pads with spaces or truncates the given double to guarantee that it is
     * exactly the desired length.
     *
     * @param val       the val to be padded
     * @param minLength the desired length of the string
     * @return a string of length containing string
     * padded with whitespace or truncated
     */
    public static String pad(double val, int minLength) {
        return pad("" + val, minLength);
    }

    /**
     * Dumps padded text. This is a simple tool for helping dump text
     * with padding to a Writer.
     *
     * @param pw      the stream to send the output
     * @param padding the number of spaces in the string
     * @param string  the string to output
     */
    public static void dump(PrintWriter pw, int padding, String string) {
        pw.print(" ".repeat(padding));
        pw.println(string);
    }

    public static String normalize(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", ""); //assumes unicode data
        //text = text.replaceAll("[^\\p{ASCII}]", "");
        return text;
    }

}

  
