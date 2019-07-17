package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 *
 */
public class SerializeTest {

    private static final Logger logger = Logger.getLogger(SerializeTest.class.getName());

    private Path serPath;

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @BeforeEach
    public void setUp() throws IOException {
        serPath = Files.createTempFile("ac", "ser");
        //
        // I just want the file name, not the actual file!
        Files.delete(serPath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        try {
        	Files.delete(serPath);
        }catch(Exception e) {}
    }

    @Test
    public void deserializeComponent() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.json");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager("stringConfig.json");
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.json");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager("stringConfig.json");
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.json");
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.json");
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

    @Test
    public void checkBadSerialisedClass() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("stringConfig.json");
            cm.setGlobalProperty("serFile", serPath.toString());
            StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
            ac.one = "one";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
                oos.writeObject(ac);
            }
            StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("badClass");
        }, "Should have thrown PropertyException for an unknown class file");
    }
}
