package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ArrayConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * A set of tests for array types using Config
 */
public class ArrayTest {

    @BeforeAll
    public void setUp() {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void arrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("arrayConfig.json");
        ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("a");
        assertArrayEquals(new int[]{1,2,3},ac.intArray, "int array not equal");
        assertArrayEquals(new long[]{9223372036854775807L,9223372036854775806L,5L},ac.longArray, "long array not equal");
        assertArrayEquals(new float[]{1.1f,2.3f,3.5f},ac.floatArray, 1e-6f, "float array not equal");
        assertArrayEquals(new double[]{1e-16,2e-16,3.16},ac.doubleArray,1e-16, "double array not equal");
    }

    @Test
    public void invalidArrayTest() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("arrayConfig.json");
            ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("invalid-char");
        }, "Invalid character array parsed, should have thrown PropertyException.");
    }

}
