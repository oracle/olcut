package com.sun.labs.util.props;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * TODO this test case is not yet implemented
 */
public class ConfigurablePropertyMapTest {

    @Test
    public void configurablePropMap() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("configurablePropMap.xml"));

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("fooMap");

        assertEquals(fm.map.get("first").name, "foo1");
        assertEquals(fm.map.get("first").value, 1);

        assertEquals(fm.map.get("second").name, "foo2");
        assertEquals(fm.map.get("second").value, 2);
    }
}
