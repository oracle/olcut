package com.oracle.labs.mlrg.olcut.util;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests the ConfigurationManager's methods for getting a URL or InputStream
 * for a file at a location.
 */
public class GetAtLocationTest {
    public static final String RESOURCE = "/com/oracle/labs/mlrg/olcut/config/allConfig.xml";
    private static final String TEXT_FILE="/com/oracle/labs/mlrg/olcut/util/textFile.txt";
    private static final String GZIP_FILE="/com/oracle/labs/mlrg/olcut/util/textFile.txt.gz";

    protected static Path tempFile;

    @BeforeAll
    public static void setup() throws IOException {
        tempFile = Files.createTempFile("olcut", "test");
    }
    
    @Test
    public void testGZipInputStream() throws IOException {
        InputStream unzippedTextStream = IOUtil.getInputStreamForLocation(GZIP_FILE);
        String unzippedText = new BufferedReader(new InputStreamReader(unzippedTextStream)).readLine();
        
        InputStream textStream = IOUtil.getInputStreamForLocation(TEXT_FILE);
        String text = new BufferedReader(new InputStreamReader(textStream)).readLine();
        assertTrue(text.equals(unzippedText), "GZipped file and plain file weren't equal");
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

    @AfterAll
    public static void teardown() {
        tempFile.toFile().delete();
    }
}
