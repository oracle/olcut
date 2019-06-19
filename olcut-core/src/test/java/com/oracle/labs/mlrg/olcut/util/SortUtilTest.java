package com.oracle.labs.mlrg.olcut.util;


import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


/**
 *
 */
public class SortUtilTest {
    private static final Logger logger = Logger.getLogger(SortUtilTest.class.getName());

    @Test
    public void testArgsort() {
        List<Double> input = Arrays.asList(0.1, 0.4, 0.35, 0.8);
        assertArrayEquals(new int[]{0, 2, 1, 3}, SortUtil.argsort(input,true), "List input not sorted ascending correctly.");
        assertArrayEquals(new int[]{3,1,2,0}, SortUtil.argsort(input,false), "List input not sorted descending correctly.");
        input = Arrays.asList(0.1, 0.35, 0.4, 0.35, 0.8, 0.8);
        assertArrayEquals(new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(input,true), "List input with duplicates not sorted ascending correctly.");
        assertArrayEquals(new int[]{4,5,2,1,3,0}, SortUtil.argsort(input,false), "List input with duplicates not sorted descending correctly.");

        double[] doubleArray = new double[]{0.1, 0.4, 0.35, 0.8};
        assertArrayEquals(new int[]{0, 2, 1, 3}, SortUtil.argsort(doubleArray,true), "double array input not sorted ascending correctly.");
        assertArrayEquals(new int[]{3,1,2,0}, SortUtil.argsort(doubleArray,false), "double array input not sorted descending correctly.");
        doubleArray = new double[]{0.1, 0.35, 0.4, 0.35, 0.8, 0.8};
        assertArrayEquals(new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(doubleArray,true), "double array input with duplicates not sorted ascending correctly.");
        assertArrayEquals(new int[]{4,5,2,1,3,0}, SortUtil.argsort(doubleArray,false), "double array input with duplicates not sorted descending correctly.");

        int[] intArray = new int[]{1, 4, 3, 8};
        assertArrayEquals(new int[]{0, 2, 1, 3}, SortUtil.argsort(intArray,true), "int array input not sorted ascending correctly.");
        assertArrayEquals(new int[]{3,1,2,0}, SortUtil.argsort(intArray,false), "int array input not sorted descending correctly.");
        intArray = new int[]{1, 3, 4, 3, 8, 8};
        assertArrayEquals(new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(intArray,true), "int array input with duplicates not sorted ascending correctly.");
        assertArrayEquals(new int[]{4,5,2,1,3,0}, SortUtil.argsort(intArray,false), "int array input with duplicates not sorted descending correctly.");
        intArray = new int[]{-1, -4, -3, 1, 3, 4, 3, 8, 8};
        assertArrayEquals(new int[]{3, 4, 6, 5, 7, 8}, SortUtil.argsort(intArray,3,intArray.length,true), "int array range input with duplicates not sorted ascending correctly.");
        assertArrayEquals(new int[]{7,8,5,4,6,3}, SortUtil.argsort(intArray,3,intArray.length,false), "int array range input with duplicates not sorted descending correctly.");
        assertArrayEquals(new int[]{1, 2, 0, 3, 4, 5}, SortUtil.argsort(intArray,0,intArray.length-3,true), "int array range input with duplicates not sorted ascending correctly.");
        assertArrayEquals(new int[]{5, 4, 3, 0, 2, 1}, SortUtil.argsort(intArray,0,intArray.length-3,false), "int array range input with duplicates not sorted descending correctly.");
    }

    @Test
    public void testWhere() {
        int[] ix = SortUtil.where(Arrays.asList(1.0, 0.5, 0.001, 0.0, -1.0, 0.01), aDouble -> aDouble > 0.);
        assertArrayEquals(new int[]{0, 1, 2, 5}, ix);
    }
}
