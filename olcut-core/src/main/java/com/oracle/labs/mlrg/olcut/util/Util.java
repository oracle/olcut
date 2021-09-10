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

package com.oracle.labs.mlrg.olcut.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Other utility functions.
 */
public class Util {
    private final static boolean TRACKING_OBJECTS = false;

    private static String mainClassName = null;

    private static long maxMemoryUsed = 0L;

    private static final String OLCUT_DIR = "OLCUT_HOME";

    /**
     * utility method for tracking object counts
     *
     * @param name  the name of the object
     * @param count the count of objects
     */
    public static void objectTracker(String name, int count) {
        if (TRACKING_OBJECTS) {
            if (count % 1000 == 0) {
                System.out.println("OT: " + name + " " + count);
            }
        }
    }

    /**
     * Dumps out memory information
     *
     * @param msg additional text for the dump
     * @return A String describing the memory usage.
     */
    public static String dumpMemoryInfo(String msg) {
        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory();
        rt.gc();
        long reclaimedMemory = (rt.freeMemory() - free)
                / (1024 * 1024);
        long freeMemory = rt.freeMemory() / (1024 * 1024);
        long totalMemory = rt.totalMemory() / (1024 * 1024);
        long usedMemory = rt.totalMemory() - rt.freeMemory();

        if (usedMemory > maxMemoryUsed) {
            maxMemoryUsed = usedMemory;
        }

        return "Memory (mb) "
                + " total: " + totalMemory
                + " reclaimed: " + reclaimedMemory
                + " free: " + freeMemory
                + " Max Used: " + (maxMemoryUsed / (1024 * 1024))
                + " -- " + msg;
    }

    /**
     * Returns the string representation of the given double value in
     * normalized scientific notation. The <code>fractionDigits</code>
     * argument gives the number of decimal digits in the fraction
     * portion. For example, if <code>fractionDigits</code> is 4, then
     * the 123450 will be "1.2345e+05". There will always be two digits
     * in the exponent portion, and a plus or minus sign before the
     * exponent.
     *
     * @param number         the double to convert
     * @param fractionDigits the number of digits in the fraction part,
     *                       e.g., 4 in "1.2345e+05".
     * @return the string representation of the double in scientific
     * notation
     */
    public static String doubleToScientificString(double number,
                                                  int fractionDigits) {
        DecimalFormat format = new DecimalFormat();

        StringBuilder buffer = new StringBuilder();
        buffer.append("0.");
        for (int i = 0; i < fractionDigits; i++) {
            buffer.append("0");
        }
        buffer.append("E00");

        String formatter = buffer.toString();

        format.applyPattern(formatter);
        String formatted = format.format(number);

        int index = formatted.indexOf('E');
        if (formatted.charAt(index + 1) != '-') {
            return formatted.substring(0, index + 1) + "+" +
                    formatted.substring(index + 1);
        } else {
            return formatted;
        }
    }

    /**
     * Compares two doubles to see if they are equal within some threshold epsilon. Originally from Knuth, Art of
     * Computer Programming. Typically the version which provides a defaulted epsilon ({@link #doubleEquals(double, double)})
     * will serve.
     * @param x a double to compare
     * @param y a double to compare
     * @param epsilon a threshold for equality
     * @return true if the two values are 'essentially equal', false otherwise
     */
    public static boolean doubleEquals(double x, double y, double epsilon) {
        return Math.abs(x - y) <= Math.max(x, y) * epsilon;
    }

    /**
     * Compares two doubles to see if they are equal within some threshold epsilon.  Originally from Knuth, Art of
     * Computer Programming. By default the threshold is one 'unit in last place'.
     * @param x a double to compare
     * @param y a double to compare
     * @return true if the two values are 'essentially equal', false otherwise
     */
    public static boolean doubleEquals(double x, double y) {
        return doubleEquals(x, y, Math.ulp(1.0));
    }

    /**
     * Implements bag/multiset equality semantics for two collections. Two collections are 'bag equals' if they contain
     * the same elements in any order.
     *
     * <p>
     *
     * N.B. Internally this uses hash maps, so equality in this case is ultimately defined by {@link Object#hashCode()}
     * rather than {@link Object#equals(Object)}.
     * @param as a collection to compare
     * @param bs a collection to compare
     * @return true if the two values are 'multiset equal', false otherwise
     */
    public static <T> boolean bagEquality(Collection<T> as, Collection<T> bs) {
        Map<T, Long> aCounts = as.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<T, Long> bCounts = bs.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return aCounts.equals(bCounts);
    }

    /**
     * Reads the next float from the given DataInputStream,
     * where the data is in little endian.
     *
     * @param dataStream the DataInputStream to read from
     * @return a float
     * @throws IOException If it failed to read from the stream.
     */
    public static float readLittleEndianFloat(DataInputStream dataStream)
            throws IOException {
        return Float.intBitsToFloat(readLittleEndianInt(dataStream));
    }


    /**
     * Reads the next little-endian integer from the given DataInputStream.
     *
     * @param dataStream the DataInputStream to read from
     * @return an integer
     * @throws IOException If it failed to read from the stream.
     */
    public static int readLittleEndianInt(DataInputStream dataStream)
            throws IOException {
        int bits = 0x00000000;
        for (int shift = 0; shift < 32; shift += 8) {
            int byteRead = (0x000000ff & dataStream.readByte());
            bits |= (byteRead << shift);
        }
        return bits;
    }


    /**
     * Byte-swaps the given integer to the other endian. That is, if this
     * integer is big-endian, it becomes little-endian, and vice-versa.
     *
     * @param integer the integer to swap
     * @return The byte-swapped integer.
     */
    public static int swapInteger(int integer) {
        return (((0x000000ff & integer) << 24) |
                ((0x0000ff00 & integer) << 8) |
                ((0x00ff0000 & integer) >> 8) |
                ((0xff000000 & integer) >> 24));
    }


    /**
     * Byte-swaps the given float to the other endian. That is, if this
     * float is big-endian, it becomes little-endian, and vice-versa.
     *
     * @param floatValue the float to swap
     * @return The byte-swapped float.
     */
    public static float swapFloat(float floatValue) {
        return Float.intBitsToFloat
                (swapInteger(Float.floatToRawIntBits(floatValue)));
    }

    /**
     * Gets the name of the Main class that started this process, or returns
     * an empty string if the main thread has exited.  The name that is
     * returned does not include the package, so called in a program started
     * from com.oracle.labs.mlrg.olcut.command.CommandInterpreter, this method
     * would return CommandInterpreter.
     *
     * @return The name of the main class in the main thread.
     */
    public static String getMainClassName() {
        if (mainClassName != null) {
            return mainClassName;
        }

        Collection<StackTraceElement[]> stacks =
                Thread.getAllStackTraces().values();
        for (StackTraceElement[] stack : stacks) {
            if (stack.length == 0) {
                continue;
            }
            StackTraceElement last = stack[stack.length - 1];
            if (last.getMethodName().equals("main")) {
                String name = last.getClassName();
                String[] comps = name.split("\\.");
                mainClassName = comps[comps.length - 1];
            }
        }
        if (mainClassName == null) {
            mainClassName = "";
        }
        return mainClassName;
    }

    /**
     * Return the local hostname.
     * @return the local hostname or localhost if it's unknown.
     */
    public static String getHostName()  {
        try {
            return java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * Return the dotfolder that olcut uses to store data. Uses environment variable
     * OLCUT_DIR if specified, `~/.olcut/` otherwise.
     * @return the path to the dotfolder
     */
    public static Path getOlcutRoot() {
        return Optional.ofNullable(System.getenv(OLCUT_DIR))
                .map(Paths::get)
                .orElse(Paths.get(System.getProperty("user.home"))
                        .resolve(".olcut"));
    }
}
