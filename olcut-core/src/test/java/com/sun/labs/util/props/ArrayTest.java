package com.sun.labs.util.props;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.assertArrayEquals;

/**
 * A set of tests for array types using Config
 */
public class ArrayTest {

    @Test
    public void arrayTest() throws IOException {
        URL cu = getClass().getResource("arrayConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("a");
        assertArrayEquals("int array not equal",new int[]{1,2,3},ac.intArray);
        assertArrayEquals("long array not equal",new long[]{9223372036854775807L,9223372036854775806L,5L},ac.longArray);
        assertArrayEquals("float array not equal",new float[]{1.1f,2.3f,3.5f},ac.floatArray, 1e-6f);
        assertArrayEquals("double array not equal",new double[]{1e-16,2e-16,3.16},ac.doubleArray,1e-16);
    }

}
