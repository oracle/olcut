package com.oracle.labs.mlrg.olcut.config.property;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GlobalPropertiesTest {

    @Test
    public void strangeSystemProperties() {
        Properties sysProps = new Properties();
        sysProps.setProperty("this property has spaces spaces are bad","some-value");
        sysProps.setProperty("this property has weird punctuation !@#$%^&&*","another-value");
        sysProps.setProperty("this.property.conforms.to.the.global.props.regex","also-a-value");

        GlobalProperties gp = new GlobalProperties();
        gp.importProperties(sysProps);
        assertNull(gp.get("this property has spaces spaces are bad"));
        assertNull(gp.get("this property has weird punctuation !@#$%^&&*"));
        assertEquals(new GlobalProperty("also-a-value"),gp.get("this.property.conforms.to.the.global.props.regex"));
    }
}
