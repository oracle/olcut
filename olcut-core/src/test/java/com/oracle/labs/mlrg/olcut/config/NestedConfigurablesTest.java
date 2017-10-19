package com.oracle.labs.mlrg.olcut.config;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Test ConfigurationManager behavior for a Configurable with 1+ inner Configurable.
 */
public class NestedConfigurablesTest {

    private static final Logger log = Logger.getLogger(NestedConfigurablesTest.class.getName());

    @Test
    public void testLoadFromXML() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("nestedConfigurablesConfig.xml"));
        FooUserConfigurable user = (FooUserConfigurable) cm.lookup("user");
        assertNotNull(user);
        FooConfigurable foo = user.getFoo();
        assertNotNull(foo);
        assertEquals("foo1", foo.name);
        assertEquals(1, foo.value);
    }

    @Test
    public void testSave() throws IOException {
        URL cu = getClass().getResource("nestedConfigurablesConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        FooUserConfigurable u1 = (FooUserConfigurable) cm1.lookup("user");
        File tmp = mkTmp();
        cm1.save(tmp);
        assertEquals(2, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(tmp.toURI().toURL());
        FooUserConfigurable u2 = (FooUserConfigurable) cm2.lookup("user");
        assertEquals(u1.getFoo(), u2.getFoo());
    }

    @Test
    public void testImportConfigurable() throws IOException {
        FooUserConfigurable u1 = new FooUserConfigurable(new FooConfigurable("foo1", 1));
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(u1);
        assertEquals(2, cm1.getNumConfigured());
        File tmp = mkTmp();
        cm1.save(tmp);
        ConfigurationManager cm2 = new ConfigurationManager(tmp.toURI().toURL());
        FooUserConfigurable u2 = (FooUserConfigurable) cm2.lookup("user");
        assertEquals(u1.getFoo(), u2.getFoo());
    }

    private static File mkTmp() throws IOException {
        File tmp = File.createTempFile("tmpConfig", ".xml");
        log.fine("created tmp file @ " + tmp.getAbsolutePath());
        tmp.deleteOnExit();
        return tmp;
    }

}
