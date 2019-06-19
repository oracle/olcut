package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    File f;

    @BeforeEach
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
        f = File.createTempFile("all-config", ".edn");
        //f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertTrue(ac!=null, "Failed to load all-config");
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac1,ac2, "Two all configs aren't equal");
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

    @Test
    public void getTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.edn");
        PropertySheet ps = cm.getPropertySheet("all-config");

        boolean boolField = (Boolean) ps.get("boolField");
        assertTrue(boolField, "Failed to lookup boolean field");

        List listStringField = (List) ps.get("listStringField");
        assertTrue(listStringField.size() == 2, "Failed to parse List<String> field");

        StringConfigurable sc = (StringConfigurable) ps.get("configurableField");
        assertEquals(new StringConfigurable("A","B","C"), sc, "StringConfigurable not constructed correctly");

        assertTrue(ps.get("monkeys") == null, "Returned a value for an invalid field name");
    }

}
