package com.sun.labs.util.props;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 *
 */
public class ConfigurablePropertyMapTest {

    public File f;

    @Before
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void configurablePropMap() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("configurablePropMap.xml"));

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("fooMap");

        assertEquals(fm.map.get("first").name, "foo1");
        assertEquals(fm.map.get("first").value, 1);

        assertEquals(fm.map.get("second").name, "foo2");
        assertEquals(fm.map.get("second").value, 2);
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        URL cu = getClass().getResource("configurablePropMap.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        URL cu = getClass().getResource("configurablePropMap.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        assertEquals(s1, s2);
    }
}
