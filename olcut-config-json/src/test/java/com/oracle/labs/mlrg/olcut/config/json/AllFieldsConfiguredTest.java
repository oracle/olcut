package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    private File f;

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @BeforeEach
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
        f = File.createTempFile("all-config", ".json");
        f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.json");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertNotNull(ac, "Failed to load all-config");
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("allConfig.json");
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac1,ac2,"Two all configs aren't equal");
    }

    @Test
    public void generateConfig() throws IOException {
        AllFieldsConfigurable ac = com.oracle.labs.mlrg.olcut.config.AllFieldsConfiguredTest.generateConfigurable();
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(ac,"all-config");
        cm1.save(f);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac,ac2, "Imported config not equal to generated object");
    }

}
