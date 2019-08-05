/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
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
     * Returns a string with the given number of
     * spaces.
     *
     * @param padding the number of spaces in the string
     * @return a string of length 'padding' containing only the SPACE
     * char.
     */
    public static String pad(int padding) {
        if (padding > 0) {
            StringBuilder sb = new StringBuilder(padding);
            for (int i = 0; i < padding; i++) {
                sb.append(' ');
            }
            return sb.toString();
        } else {
            return "";
        }
    }

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
            result = string + pad(minLength - string.length());
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
        pw.print(pad(padding));
        pw.println(string);
    }

    public static String normalize(String text) {
    	text = Normalizer.normalize(text, Normalizer.Form.NFD);
    	text = text.replaceAll("\\p{M}", ""); //assumes unicode data
//    	text = text.replaceAll("[^\\p{ASCII}]", "");
    	return text;
    }

}

  
