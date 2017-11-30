package com.oracle.labs.mlrg.olcut.util;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

/**
 *
 */
public class SortUtilTest {
    private static final Logger logger = Logger.getLogger(SortUtilTest.class.getName());

    @Test
    public void testArgsort() {
        List<Double> input = Arrays.asList(0.1, 0.4, 0.35, 0.8);
        assertArrayEquals("List input not sorted ascending correctly.", new int[]{0, 2, 1, 3}, SortUtil.argsort(input,true));
        assertArrayEquals("List input not sorted descending correctly.", new int[]{3,1,2,0}, SortUtil.argsort(input,false));
        input = Arrays.asList(0.1, 0.35, 0.4, 0.35, 0.8, 0.8);
        assertArrayEquals("List input with duplicates not sorted ascending correctly.", new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(input,true));
        assertArrayEquals("List input with duplicates not sorted descending correctly.", new int[]{4,5,2,1,3,0}, SortUtil.argsort(input,false));

        double[] doubleArray = new double[]{0.1, 0.4, 0.35, 0.8};
        assertArrayEquals("double array input not sorted ascending correctly.", new int[]{0, 2, 1, 3}, SortUtil.argsort(doubleArray,true));
        assertArrayEquals("double array input not sorted descending correctly.", new int[]{3,1,2,0}, SortUtil.argsort(doubleArray,false));
        doubleArray = new double[]{0.1, 0.35, 0.4, 0.35, 0.8, 0.8};
        assertArrayEquals("double array input with duplicates not sorted ascending correctly.", new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(doubleArray,true));
        assertArrayEquals("double array input with duplicates not sorted descending correctly.", new int[]{4,5,2,1,3,0}, SortUtil.argsort(doubleArray,false));

        int[] intArray = new int[]{1, 4, 3, 8};
        assertArrayEquals("int array input not sorted ascending correctly.", new int[]{0, 2, 1, 3}, SortUtil.argsort(intArray,true));
        assertArrayEquals("int array input not sorted descending correctly.", new int[]{3,1,2,0}, SortUtil.argsort(intArray,false));
        intArray = new int[]{1, 3, 4, 3, 8, 8};
        assertArrayEquals("int array input with duplicates not sorted ascending correctly.", new int[]{0, 1, 3, 2, 4, 5}, SortUtil.argsort(intArray,true));
        assertArrayEquals("int array input with duplicates not sorted descending correctly.", new int[]{4,5,2,1,3,0}, SortUtil.argsort(intArray,false));
        intArray = new int[]{-1, -4, -3, 1, 3, 4, 3, 8, 8};
        assertArrayEquals("int array range input with duplicates not sorted ascending correctly.", new int[]{3, 4, 6, 5, 7, 8}, SortUtil.argsort(intArray,3,intArray.length,true));
        assertArrayEquals("int array range input with duplicates not sorted descending correctly.", new int[]{7,8,5,4,6,3}, SortUtil.argsort(intArray,3,intArray.length,false));
        assertArrayEquals("int array range input with duplicates not sorted ascending correctly.", new int[]{1, 2, 0, 3, 4, 5}, SortUtil.argsort(intArray,0,intArray.length-3,true));
        assertArrayEquals("int array range input with duplicates not sorted descending correctly.", new int[]{5, 4, 3, 0, 2, 1}, SortUtil.argsort(intArray,0,intArray.length-3,false));
    }

    @Test
    public void testWhere() {
        int[] ix = SortUtil.where(Arrays.asList(1.0, 0.5, 0.001, 0.0, -1.0, 0.01), aDouble -> aDouble > 0.);
        assertArrayEquals(new int[]{0, 1, 2, 5}, ix);
    }
}
