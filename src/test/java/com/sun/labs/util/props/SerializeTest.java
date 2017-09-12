package com.sun.labs.util.props;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Path serPath;

    public SerializeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        serPath = Files.createTempFile("ac", "ser");
        //
        // I just want the file name, not the actual file!
        Files.delete(serPath);
    }

    @After
    public void tearDown() throws IOException {
        Files.delete(serPath);
    }

    @Test
    public void deserializeComponent() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "one");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "c");
    }

    /**
     * Tests whether when we de-serialize a component and then ask for it again
     * that we're not deserializing it a second time.
     */
    @Test
    public void deserializeComponentAndReuse() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "one");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "c");
        ac.one = "two";
        ac.three = "three";
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "two");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "three");
        
    }

    @Test
    public void deserializeObject() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "one");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "c");
    }

    @Test
    public void deserializeObjectAndReuse() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("stringConfig.xml"));
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "one");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "c");
        acs.one = "two";
        acs.three = "three";
        acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "two");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "three");
    }
}
