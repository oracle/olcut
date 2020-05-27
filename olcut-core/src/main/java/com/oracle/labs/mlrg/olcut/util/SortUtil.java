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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Utilities for sorting arrays or lists while remembering the original indices.
 */
public final class SortUtil {

    private SortUtil() {}

    /**
     * Finds the indices where the predicate is true.
     * @param input The input to check.
     * @param func The predicate.
     * @return An array containing indices where the predicate is true in the input array.
     */
    public static int[] where(int[] input, IntPredicate func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    /**
     * Finds the indices where the predicate is true.
     * @param input The input to check.
     * @param func The predicate.
     * @return An array containing indices where the predicate is true in the input array.
     */
    public static int[] where(double[] input, DoublePredicate func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    /**
     * Finds the indices where the predicate is true.
     * @param input The input to check.
     * @param func The predicate.
     * @param <T> The type of the list.
     * @return An array containing indices where the predicate is true in the input array.
     */
    public static <T> int[] where(List<T> input, Predicate<T> func) {
        Integer[] ixs = new Integer[input.size()];
        IntStream.range(0, input.size()).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input.get(i))).mapToInt(i -> i).toArray();
    }

    /**
     * Finds the indices where the predicate is true.
     * @param input The input to check.
     * @param func The predicate.
     * @param <T> The type of the array.
     * @return An array containing indices where the predicate is true in the input array.
     */
    public static <T> int[] where(T[] input, Predicate<T> func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    /**
     * Returns the indices in the order which makes the input sorted.
     *
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * @param input The input to sort.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static int[] argsort(int[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

    /*
     * This was found online as an equivalent but much more succinct solution.
     * 
     * int[] sortedIndexes = IntStream.range(0, postingIds.length).boxed().sorted((i, j) -> Integer.compare(postingIds[i], postingIds[j])).mapToInt(ele -> ele).toArray();
     */

    /**
     * Returns the indices in the order which makes the input sorted.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * <p>
     * Only returns the indices from the sub range, so in the original input all the
     * appropriate values are stored at input[output[i]+start].
     * @param input The input to sort.
     * @param start The starting point.
     * @param end The ending point.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static int[] argsort(int[] input, int start, int end, boolean ascending) {
        SortIntegerTuple[] array = new SortIntegerTuple[end-start];
        for (int i = start; i < end; i++) {
            array[i-start] = new SortIntegerTuple(ascending,input[i],i);
        }
        Arrays.sort(array);
        int[] output = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = array[i].index;
        }
        return output;
    }

    /**
     * A tuple of two ints and a boolean. It wants to be a record. Or a value type.
     * <p>
     * Used to provide something comparable for {@link SortUtil#argsort}.
     */
    private static class SortIntegerTuple implements Comparable<SortIntegerTuple> {
        private final boolean ascending;
        public final int value;
        public final int index;

        public SortIntegerTuple(boolean ascending, int value, int index) {
            this.ascending = ascending;
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(SortIntegerTuple o) {
            if (ascending) {
                return Integer.compare(value, o.value);
            } else {
                return Integer.compare(o.value, value);
            }
        }
    }

    /**
     * Returns the indices in the order which makes the input sorted.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * @param input The input to sort.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static int[] argsort(double[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

    /**
     * Returns the indices in the order which makes the input sorted.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * <p>
     * Only returns the indices from the sub range, so in the original input all the
     * appropriate values are stored at input[output[i]+start].
     * @param input The input to sort.
     * @param start The starting point.
     * @param end The ending point.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static int[] argsort(double[] input, int start, int end, boolean ascending) {
        SortDoubleTuple[] array = new SortDoubleTuple[end-start];
        for (int i = start; i < end; i++) {
            array[i-start] = new SortDoubleTuple(ascending,input[i],i);
        }
        Arrays.sort(array);
        int[] output = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = array[i].index;
        }
        return output;
    }

    /**
     * A tuple of an int, a double and a boolean. It wants to be a record. Or a value type.
     * <p>
     * Used to provide something comparable for {@link SortUtil#argsort}.
     */
    private static class SortDoubleTuple implements Comparable<SortDoubleTuple> {
        private final boolean ascending;
        public final double value;
        public final int index;

        public SortDoubleTuple(boolean ascending, double value, int index) {
            this.ascending = ascending;
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(SortDoubleTuple o) {
            if (ascending) {
                return Double.compare(value, o.value);
            } else {
                return Double.compare(o.value, value);
            }
        }
    }

    /**
     * Returns the indices in the order which makes the input sorted according to it's natural ordering.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * @param input The input to sort.
     * @param ascending Should the sort be ascending or descending.
     * @param <T> The type of the list.
     * @return The indices in sorted order.
     */
    public static <T extends Comparable<T>> int[] argsort(List<T> input, boolean ascending) {
        return argsort(input,0,input.size(),ascending);
    }

    /**
     * Returns the indices in the order which makes the input sorted according to it's natural ordering.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * <p>
     * Only returns the indices from the sub range, so in the original input all the
     * appropriate values are stored at input[output[i]+start].
     * @param input The input to sort.
     * @param start The starting point.
     * @param end The ending point.
     * @param <T> The type of the list.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static <T extends Comparable<T>> int[] argsort(List<T> input, int start, int end, boolean ascending) {
        List<SortTuple<T>> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            list.add(new SortTuple<>(ascending,input.get(i),i));
        }
        Collections.sort(list);
        int[] output = new int[list.size()];
        int i = 0;
        for (SortTuple<T> e : list) {
            output[i] = e.index;
            i++;
        }
        return output;
    }

    /**
     * Returns the indices in the order which makes the input sorted according to it's natural ordering.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * @param input The input to sort.
     * @param ascending Should the sort be ascending or descending.
     * @param <T> The type of the array.
     * @return The indices in sorted order.
     */
    public static <T extends Comparable<T>> int[] argsort(T[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

    /**
     * Returns the indices in the order which makes the input sorted according to it's natural ordering.
     * <p>
     * For example, if the input is [4,1,3,2] then the output is [1,3,2,0],
     * as then iterating input[output[i]] gives input in the sorted order.
     * <p>
     * Only returns the indices from the sub range, so in the original input all the
     * appropriate values are stored at input[output[i]+start].
     * @param input The input to sort.
     * @param start The starting point.
     * @param end The ending point.
     * @param <T> The type of the list.
     * @param ascending Should the sort be ascending or descending.
     * @return The indices in sorted order.
     */
    public static <T extends Comparable<T>> int[] argsort(T[] input, int start, int end, boolean ascending) {
        List<SortTuple<T>> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            list.add(new SortTuple<>(ascending,input[i],i));
        }
        Collections.sort(list);
        int[] output = new int[list.size()];
        int i = 0;
        for (SortTuple<T> e : list) {
            output[i] = e.index;
            i++;
        }
        return output;
    }

    /**
     * A tuple of an int, a T and a boolean. It wants to be a record. Or a value type.
     * <p>
     * Used to provide something comparable for {@link SortUtil#argsort}.
     */
    private static class SortTuple<T extends Comparable<T>> implements Comparable<SortTuple<T>> {
        private final boolean ascending;
        public final T value;
        public final int index;

        public SortTuple(boolean ascending, T value, int index) {
            this.ascending = ascending;
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(SortTuple<T> o) {
            if (ascending) {
                return value.compareTo(o.value);
            } else {
                return o.value.compareTo(value);
            }
        }
    }
}
