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
 *
 */
public abstract class SortUtil {

    public static int[] where(int[] input, IntPredicate func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    public static int[] where(double[] input, DoublePredicate func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    public static <T> int[] where(List<T> input, Predicate<T> func) {
        Integer[] ixs = new Integer[input.size()];
        IntStream.range(0, input.size()).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input.get(i))).mapToInt(i -> i).toArray();
    }

    public static <T> int[] where(T[] input, Predicate<T> func) {
        Integer[] ixs = new Integer[input.length];
        IntStream.range(0, input.length).forEach(i -> ixs[i] = i);
        return Arrays.stream(ixs).filter(i -> func.test(input[i])).mapToInt(i -> i).toArray();
    }

    public static int[] argsort(int[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

    /*
     * This was found online as an equivalent but much more succinct solution.
     * 
     * int[] sortedIndexes = IntStream.range(0, postingIds.length).boxed().sorted((i, j) -> Integer.compare(postingIds[i], postingIds[j])).mapToInt(ele -> ele).toArray();
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

    public static int[] argsort(double[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

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

    public static <T extends Comparable<T>> int[] argsort(List<T> input, boolean ascending) {
        return argsort(input,0,input.size(),ascending);
    }

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

    public static <T extends Comparable<T>> int[] argsort(T[] input, boolean ascending) {
        return argsort(input,0,input.length,ascending);
    }

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
