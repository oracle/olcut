package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ArrayConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * A set of tests for array types using Config
 */
public class ArrayTest {

    @Test
    public void arrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("arrayConfig.json");
        ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("a");
        assertArrayEquals("int array not equal",new int[]{1,2,3},ac.intArray);
        assertArrayEquals("long array not equal",new long[]{9223372036854775807L,9223372036854775806L,5L},ac.longArray);
        assertArrayEquals("float array not equal",new float[]{1.1f,2.3f,3.5f},ac.floatArray, 1e-6f);
        assertArrayEquals("double array not equal",new double[]{1e-16,2e-16,3.16},ac.doubleArray,1e-16);
    }

    @Test(expected = PropertyException.class)
    public void invalidArrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("arrayConfig.json");
        ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("invalid-char");
        Assert.fail("Invalid character array parsed, should have thrown PropertyException.");
    }

}
