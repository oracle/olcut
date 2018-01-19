package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MBeanTest {

    public MBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.unregisterMBean(new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=registerTest"));
        } catch(InstanceNotFoundException ex) {
            //
            // This is OK, there might have been a failure in the test.
        } catch(Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, "Exception removing mbean", ex);
        }

        try {
            mbs.unregisterMBean(
                    new ObjectName(
                    "com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest"));
        } catch(InstanceNotFoundException ex) {
            //
            // This is OK, there might have been a failure in the test.
        } catch(Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, "Exception removing mbean",
                                     ex);
        }
    }

    @Test
    public void registerMBean() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("registerTest");
        assertNotNull("Couldn't lookup registerTest", smbc);
        assertEquals(smbc.getValue("a"), "10");
    }

    @Test
    public void retrieveProperty() throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("registerTest");
        assertNotNull("Couldn't lookup registerTest", smbc);

        MBeanServer mbs = cm.getMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=registerTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
    }

    @Test
    public void retrieveList()
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull("Couldn't lookup listTest", smbc);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"baz", "quux"}, cmxb.getValues("c"));
    }

    @Test
    public void modifyProperty() 
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull("Couldn't lookup listTest", smbc);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertTrue("Unable to set a!", cmxb.setValue("a", "100"));
        assertTrue("Unable to set b!", cmxb.setValue("b", "bargle"));
        assertEquals("100", cmxb.getValue("a"));
        assertEquals("bargle", cmxb.getValue("b"));
    }
    
    @Test
    public void modifyList()
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull("Couldn't lookup listTest", smbc);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"baz", "quux"}, cmxb.getValues("c"));
        assertTrue("Unable to set c!", cmxb.setValues("c", new String[] {"props", "rule", "stuff"}));
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"props", "rule", "stuff"}, cmxb.getValues("c"));
    }

    @Test
    public void modifyAll()
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull("Couldn't lookup listTest", smbc);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"baz", "quux"}, cmxb.getValues("c"));
        assertTrue("Unable to set a!", cmxb.setValue("a", "100"));
        assertTrue("Unable to set b!", cmxb.setValue("b", "bargle"));
        assertTrue("Unable to set c!", cmxb.setValues("c", new String[] {"props", "rule", "stuff"}));
        assertEquals("100", cmxb.getValue("a"));
        assertEquals("bargle", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"props", "rule", "stuff"}, cmxb.getValues("c"));
    }
}