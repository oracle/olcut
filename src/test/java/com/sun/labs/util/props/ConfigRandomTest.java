package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests the construction of {@link java.util.Random} objects from a {@link PropertySheet}.
 */
public class ConfigRandomTest {
    
    public ConfigRandomTest() { }

    @Test
    public void configRandom() throws IOException {
        URL cu = getClass().getResource("randomConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        RandomConfigurable r = (RandomConfigurable) cm.lookup("random");
        int first = r.one.nextInt();
        int second = r.two.nextInt();
        assertEquals(-1157793070,first);
        assertEquals(-1150867590,second);
    }
}
