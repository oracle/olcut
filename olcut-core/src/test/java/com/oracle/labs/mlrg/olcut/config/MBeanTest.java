/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MBeanTest {

    public MBeanTest() {
    }

    @AfterEach
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
        assertNotNull(smbc,"Couldn't lookup registerTest");
        assertEquals(smbc.getValue("a"), "10");
    }

    @Test
    public void retrieveProperty() throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("registerTest");
        assertNotNull(smbc, "Couldn't lookup registerTest");

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
        assertNotNull(smbc, "Couldn't lookup listTest");

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
        assertNotNull(smbc, "Couldn't lookup listTest");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertTrue(cmxb.setValue("a", "100"),"Unable to set a!");
        assertTrue(cmxb.setValue("b", "bargle"),"Unable to set b!");
        assertEquals("100", cmxb.getValue("a"));
        assertEquals("bargle", cmxb.getValue("b"));
    }
    
    @Test
    public void modifyList()
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull(smbc, "Couldn't lookup listTest");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"baz", "quux"}, cmxb.getValues("c"));
        assertTrue(cmxb.setValues("c", new String[] {"props", "rule", "stuff"}), "Unable to set c!");
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"props", "rule", "stuff"}, cmxb.getValues("c"));
    }

    @Test
    public void modifyAll()
            throws IOException, NullPointerException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
        ConfigurationManager cm = new ConfigurationManager("mbeanConfig.xml");
        SimpleMBConfigurable smbc = (SimpleMBConfigurable) cm.lookup("listTest");
        assertNotNull(smbc, "Couldn't lookup listTest");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName("com.oracle.labs.mlrg.olcut.config:type=SimpleMBConfigurable,name=listTest");
        ConfigurableMXBean cmxb = JMX.newMBeanProxy(mbs, oname, ConfigurableMXBean.class);
        assertEquals("10", cmxb.getValue("a"));
        assertEquals("test", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"baz", "quux"}, cmxb.getValues("c"));
        assertTrue(cmxb.setValue("a", "100"),"Unable to set a!");
        assertTrue(cmxb.setValue("b", "bargle"),"Unable to set b!");
        assertTrue(cmxb.setValues("c", new String[] {"props", "rule", "stuff"}),"Unable to set c!");
        assertEquals("100", cmxb.getValue("a"));
        assertEquals("bargle", cmxb.getValue("b"));
        assertArrayEquals(new String[] {"props", "rule", "stuff"}, cmxb.getValues("c"));
    }
}