package com.oracle.labs.mlrg.olcut.config;


import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A test for using the @Config annotation.
 */
public class TypeConfigTest {
    
    public TypeConfigTest() {
    }
    

    @Test
    public void defaultValues() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.xml");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("default");
        assertEquals(bc1.s, "default");
        assertEquals(bc1.i, 16);
        assertEquals(bc1.bigI.intValue(), 17);
        assertEquals(bc1.l, 18);
        assertEquals(bc1.bigL.longValue(), 19);
        assertEquals(bc1.d, 21.0, 1E-9);
        assertEquals(bc1.bigD, 22.0, 1E-9);
    }

    @Test
    public void configuredTypes() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.xml");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        assertEquals(bc1.s, "one");
        assertEquals(bc1.i, 2);
        assertEquals(bc1.d, 3.0, 1E-9);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        assertEquals(bc1.s, "two");
        assertEquals(bc1.i, 3);
        assertEquals(bc1.d, 6.3, 1E-9);
    }
    
    @Test
    public void listTypes() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.xml");
        ListTypeConfigurable lc1 = (ListTypeConfigurable) cm1.lookup("l1");
        Configurable[] cl = lc1.getList();
        BasicConfigurable bc1 = (BasicConfigurable) cl[0];
        assertEquals(bc1.s, "default");
        assertEquals(bc1.i, 16);
        assertEquals(bc1.bigI.intValue(), 17);
        assertEquals(bc1.l, 18);
        assertEquals(bc1.bigL.longValue(), 19);
        assertEquals(bc1.d, 21.0, 1E-9);
        assertEquals(bc1.bigD, 22.0, 1E-9);
        bc1 = (BasicConfigurable) cl[1];
        assertEquals(bc1.s, "one");
        assertEquals(bc1.i, 2);
        assertEquals(bc1.d, 3.0, 1E-9);
        bc1 = (BasicConfigurable) cl[2];
        assertEquals(bc1.s, "two");
        assertEquals(bc1.i, 3);
        assertEquals(bc1.d, 6.3, 1E-9);
               
    }

}
