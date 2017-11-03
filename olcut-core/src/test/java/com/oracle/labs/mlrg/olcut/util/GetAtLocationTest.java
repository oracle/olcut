package com.oracle.labs.mlrg.olcut.util;

import com.oracle.labs.mlrg.olcut.util.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the ConfigurationManager's methods for getting a URL or InputStream
 * for a file at a location.
 */
public class GetAtLocationTest {
    public static final String RESOURCE = "/com/oracle/labs/mlrg/olcut/config/allConfig.xml";
    protected static Path tempFile;

    @BeforeClass
    public static void setup() throws IOException {
        tempFile = Files.createTempFile("olcut", "test");
    }

    @Test
    public void getResourceURL() throws URISyntaxException {
        URL url = IOUtil.getURLForLocation(RESOURCE);
        assertTrue(new File(url.toURI()).exists());
    }

    @Test
    public void getResourceInputStream() throws IOException {
        InputStream is = IOUtil.getInputStreamForLocation(RESOURCE);
        assertNotNull(is);
    }

    @Test
    public void getURLURL() throws MalformedURLException, URISyntaxException {
        URL url = IOUtil.getURLForLocation(tempFile.toUri().toURL().toString());
        assertTrue(new File(url.toURI()).exists());
    }

    @Test
    public void getURLInputStream() throws IOException {
        InputStream is = IOUtil.getInputStreamForLocation(tempFile.toUri().toURL().toString());
        assertNotNull(is);
    }

    @Test
    public void getFileURL() throws URISyntaxException {
        URL url = IOUtil.getURLForLocation(tempFile.toString());
        assertTrue(new File(url.toURI()).exists());
    }

    @Test
    public void getFileInputStream() {
        InputStream is = IOUtil.getInputStreamForLocation(tempFile.toString());
        assertNotNull(is);
    }

    @AfterClass
    public static void teardown() {
        tempFile.toFile().delete();
    }
}
