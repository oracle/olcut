package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
        f = File.createTempFile("all-config", ".json");
        //f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.json");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertTrue("Failed to load all-config",ac!=null);
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("allConfig.json");
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

    @Test
    public void getTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.json");
        PropertySheet ps = cm.getPropertySheet("all-config");

        boolean boolField = (Boolean) ps.get("boolField");
        assertTrue("Failed to lookup boolean field", boolField);

        List listStringField = (List) ps.get("listStringField");
        assertTrue("Failed to parse List<String> field", listStringField.size() == 2);

        StringConfigurable sc = (StringConfigurable) ps.get("configurableField");
        assertEquals("StringConfigurable not constructed correctly",new StringConfigurable("A","B","C"), sc);

        assertTrue("Returned a value for an invalid field name", ps.get("monkeys") == null);
    }

}
