package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.FooMapConfigurable;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class ConfigurablePropertyMapTest {

    public File f;

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".edn");
        f.deleteOnExit();
    }

    @Test
    public void configurablePropMap() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("configurablePropMap.edn");

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("fooMap");

        assertEquals(fm.map.get("first").name, "foo1");
        assertEquals(fm.map.get("first").value, 1);

        assertEquals(fm.map.get("second").name, "foo2");
        assertEquals(fm.map.get("second").value, 2);
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("configurablePropMap.edn");
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("configurablePropMap.edn");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }
}
