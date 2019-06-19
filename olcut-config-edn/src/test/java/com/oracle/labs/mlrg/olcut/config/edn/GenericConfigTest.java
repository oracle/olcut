package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.ListConfig;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SetConfig;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class GenericConfigTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void correctListConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.edn");
        ListConfig s = (ListConfig) cm.lookup("correctListConfig");

        assertEquals(4,s.stringList.size(), "StringList has an incorrect number of values");
        assertTrue(s.stringList.contains("5.5"), "StringList missing values");
        assertTrue(s.stringList.contains("3.14"), "StringList missing values");
        assertTrue(s.stringList.contains("2.79"), "StringList missing values");
        assertFalse(s.stringList.contains("1000000.0"), "StringList contains incorrect value");

        assertEquals(4,s.doubleList.size(), "DoubleList has an incorrect number of values");
        assertTrue(s.doubleList.contains(5.5), "DoubleList missing values");
        assertTrue(s.doubleList.contains(3.14), "DoubleList missing values");
        assertTrue(s.doubleList.contains(2.79), "DoubleList missing values");
        assertFalse(s.doubleList.contains(1000000.0), "DoubleList contains incorrect values");

        assertEquals(2,s.stringConfigurableList.size(), "StringConfigList has an incorrect number of values");
        assertTrue(s.stringConfigurableList.contains(new StringConfigurable("dragons","wyverns","wyrms")), "StringConfigList missing values");
        assertTrue(s.stringConfigurableList.contains(new StringConfigurable("jedi","sith","scoundrels")), "StringConfigList missing values");
        assertFalse(s.stringConfigurableList.contains(new StringConfigurable("sheep","cows","pigs")), "StringConfigList contains incorrect values");
    }

    @Test
    public void incorrectListConfig() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("genericConfig.edn");
            ListConfig l = (ListConfig) cm.lookup("incorrectListConfig");
        });
    }

    @Test
    public void correctSetConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.edn");
        SetConfig s = (SetConfig) cm.lookup("correctSetConfig");

        assertEquals(3,s.stringSet.size(), "StringSet has an incorrect number of values");
        assertTrue(s.stringSet.contains("5.5"), "StringSet missing values");
        assertTrue(s.stringSet.contains("3.14"), "StringSet missing values");
        assertTrue(s.stringSet.contains("2.79"), "StringSet missing values");
        assertFalse(s.stringSet.contains("1000000.0"), "StringSet contains incorrect value");

        assertEquals(3,s.doubleSet.size(), "DoubleSet has an incorrect number of values");
        assertTrue(s.doubleSet.contains(5.5), "DoubleSet missing values");
        assertTrue(s.doubleSet.contains(3.14), "DoubleSet missing values");
        assertTrue(s.doubleSet.contains(2.79), "DoubleSet missing values");
        assertFalse(s.doubleSet.contains(1000000.0), "DoubleSet contains incorrect values");

        assertEquals(2,s.stringConfigurableSet.size(), "StringConfigSet has an incorrect number of values");
        assertTrue(s.stringConfigurableSet.contains(new StringConfigurable("dragons","wyverns","wyrms")), "StringConfigSet missing values");
        assertTrue(s.stringConfigurableSet.contains(new StringConfigurable("jedi","sith","scoundrels")), "StringConfigSet missing values");
        assertFalse(s.stringConfigurableSet.contains(new StringConfigurable("sheep","cows","pigs")), "StringConfigSet contains incorrect values");
    }

    @Test
    public void incorrectSetConfig() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("genericConfig.edn");
            SetConfig s = (SetConfig) cm.lookup("incorrectSetConfig");
        });
    }
}
