/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
        URL url = IOUtil.getURLForLocation(TEXT_FILE);
        assertTrue(new File(url.toURI()).exists());
    }

    @Test
    public void getResourceInputStream() throws IOException {
        InputStream is = IOUtil.getInputStreamForLocation(TEXT_FILE);
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
