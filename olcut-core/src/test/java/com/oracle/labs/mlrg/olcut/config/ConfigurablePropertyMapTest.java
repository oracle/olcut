package com.oracle.labs.mlrg.olcut.config;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class ConfigurablePropertyMapTest {

    public File f;

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void configurablePropMap() {
        ConfigurationManager cm = new ConfigurationManager("configurablePropMap.xml");

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("fooMap");

        assertEquals("foo1", fm.map.get("first").name);
        assertEquals(1, fm.map.get("first").value);

        assertEquals("foo2",fm.map.get("second").name);
        assertEquals(2,fm.map.get("second").value);
    }

    @Test
    public void overriddenPropMap() {
        ConfigurationManager cm = new ConfigurationManager("configurablePropMap.xml");

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("overriddenMap");

        assertEquals("foo3", fm.map.get("first").name);
        assertEquals(20, fm.map.get("first").value);

        assertEquals("foo2", fm.map.get("second").name);
        assertEquals(2, fm.map.get("second").value);
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("configurablePropMap.xml");
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("configurablePropMap.xml");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }
}
