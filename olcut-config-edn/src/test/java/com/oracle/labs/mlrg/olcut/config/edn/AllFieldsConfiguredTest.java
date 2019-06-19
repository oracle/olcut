package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    File f;

    @Before
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
        f = File.createTempFile("all-config", ".edn");
        //f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertTrue("Failed to load all-config",ac!=null);
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals("Two all configs aren't equal",ac1,ac2);
    }

    @Test
    public void generateConfig() throws IOException {
        AllFieldsConfigurable ac = com.oracle.labs.mlrg.olcut.config.AllFieldsConfiguredTest.generateConfigurable();
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(ac,"all-config");
        cm1.save(f);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals("Imported config not equal to generated object",ac,ac2);
    }
}
