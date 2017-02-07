/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.util.props;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class SerializeTest {
    
    private static final Logger logger = Logger.getLogger(SerializeTest.class.getName());
    
    private static String serFile = "/tmp/ac.ser";
    
    public SerializeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        try {
            Files.delete(Paths.get(serFile));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error removing " + serFile, ex);
        }
    }
    
    @Test
    public void deserialize() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        StringConfig ac = (StringConfig) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        ac = (StringConfig) cm.lookup("ac");
        assertEquals(ac.one, "one");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "c");
    }

}
