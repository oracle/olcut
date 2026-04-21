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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A collection of IO helper functions, in need of refactoring and sanitization.
 */
public final class IOUtil {
    private static final Logger logger = Logger.getLogger(IOUtil.class.getName());
    public static final int BUFFER_SIZE = 1000000;
    private static final Pattern linefeedPattern = Pattern.compile("\\R");

    private IOUtil() {}

    public static List<String> getLinesFromString(String text) {
        String[] lines = linefeedPattern.split(text);
        return new ArrayList<>(Arrays.asList(lines));
    }

    public static List<String> getLines(String path) throws IOException {
        return getLines(path, -1);
    }

    public static List<String> getLines(String path, Charset encoding) throws IOException {
        return getLines(path, -1, encoding);
    }

    public static List<String> getLines(String path, int count) throws IOException {
        return getLines(path, count, StandardCharsets.UTF_8);
    }

    public static List<String> getLines(String path, int count, Charset encoding) throws IOException {
        try (BufferedReader reader = getReader(path, encoding)) {
            return getLines(reader,count);
        }
    }

    public static List<String> getLines(BufferedReader reader, int count) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
            if (count > 0 && lines.size() == count) {
                return lines;
            }
        }
        return lines;
    }

    /**
     * Loads the string as a classpath resource or a path, using UTF-8.
     * @param path The path to load.
     * @return A buffered reader.
     * @throws FileNotFoundException If the path wasn't found.
     */
    public static BufferedReader getReader(String path) throws FileNotFoundException {
        return getReader(path, StandardCharsets.UTF_8);
    }

    /**
     * Loads the string as a classpath resource or a path, using the specified charset.
     * @param path The path to load.
     * @param charset The charset to use.
     * @return A buffered reader.
     * @throws FileNotFoundException If the path wasn't found.
     */
    public static BufferedReader getReader(String path, Charset charset) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(getInputStream(path),charset),BUFFER_SIZE);
    }

    /**
     * This method converts the URI into a URL, and applies the
     * protocol check to see if it's http or https. If it is then an exception is thrown, otherwise
     * the stream is opened. Figures out if the stream is zipped using the magic bytes.
     * @param uri The URI to load.
     * @param charset The charset to use.
     * @return A buffered reader from the input stream.
     * @throws IOException If the URI failed to open.
     */
    public static BufferedReader getReader(URI uri, Charset charset) throws IOException {
        return getReader(uri.toURL(),charset);
    }

    /**
     * This method applies the
     * protocol check to see if the URL is http or https. If it is then an exception is thrown, otherwise
     * the stream is opened. Figures out if the stream is zipped using the magic bytes.
     * @param url The URL to load.
     * @param charset The charset to use.
     * @return A buffered reader from the input stream.
     * @throws IOException If the URI failed to open.
     */
    public static BufferedReader getReader(URL url, Charset charset) throws IOException {
        if (isDisallowedProtocol(url)) {
            throw new IllegalArgumentException("Tried to read disallowed URL protocol: '" + url.toString() + "'");
        }
        return getReader(url.openStream(),charset);
    }

    /**
     * Opens a buffered reader on the specified path with the specified charset. Figures out if the stream is zipped using the magic bytes.
     * @param path The path to read.
     * @param charset The charset to use.
     * @return A buffered reader.
     * @throws IOException If the path failed to open.
     */
    public static BufferedReader getReader(Path path, Charset charset) throws IOException {
        return getReader(new FileInputStream(path.toFile()),charset);
    }

    /**
     * Makes a reader wrapped around the string. Figures out if the stream is zipped using the magic bytes.
     * @param filename The input filename.
     * @param charset The charset to use.
     * @return A BufferedReader wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be read.
     * @throws IOException If an error occurred when opening the file.
     */
    public static BufferedReader getReader(String filename, String charset) throws FileNotFoundException, IOException {
        return getReader(new File(filename), charset);
    }

    /**
     * Makes a reader wrapped around the file. Figures out if the stream is zipped using the magic bytes.
     * @param file The file to read.
     * @param charset The charset to use.
     * @return A BufferedReader wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be read.
     * @throws IOException If an error occurred when opening the file.
     */
    public static BufferedReader getReader(File file, String charset) throws FileNotFoundException, IOException {
        return getReader(new FileInputStream(file),Charset.forName(charset));
    }

    private static BufferedReader getReader(InputStream stream, Charset charset) throws IOException {
        InputStream wrappedStream = wrapGZIPStream(stream);
        return new BufferedReader(new InputStreamReader(wrappedStream,charset),BUFFER_SIZE);
    }

    /**
     * Makes a writer wrapped around the specified file. Either zipped or not.
     * @param filename The output filename.
     * @param zipped Is the file zipped?
     * @return A PrintWriter wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be written.
     * @throws IOException If an error occurred when opening the file.
     */
    public static PrintWriter getPrintWriter(String filename, boolean zipped) throws FileNotFoundException, IOException {
        return new PrintWriter(new OutputStreamWriter(innerGetOutputStream(filename,zipped),StandardCharsets.UTF_8));
    }

    /**
     * Makes a writer wrapped around the string. Either zipped or not.
     * @param filename The output filename.
     * @param zipped Is the file zipped?
     * @return A PrintWriter wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be written.
     * @throws IOException If an error occurred when opening the file for writing.
     */
    public static ObjectOutputStream getObjectOutputStream(String filename, boolean zipped) throws FileNotFoundException, IOException {
        return new ObjectOutputStream(innerGetOutputStream(filename,zipped));
    }

    /**
     * Makes a {@link BufferedOutputStream}, optionally wrapping it in a {@link GZIPOutputStream}.
     * @param filename The output filename.
     * @param zipped Should the file be zipped.
     * @return A BufferedOutputStream.
     * @throws FileNotFoundException If the file can't be written.
     * @throws IOException If an error occurred when opening the file for writing.
     */
    private static OutputStream innerGetOutputStream(String filename, boolean zipped) throws FileNotFoundException, IOException {
        if (zipped) {
            return new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE));
        } else {
            return new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
        }
    }

    /**
     * Makes a ObjectInputStream wrapped around the file. Figures out if the stream is zipped using the magic bytes.
     * @param filename The input filename.
     * @return A ObjectInputStream wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be read.
     * @throws IOException If an error occurred when opening the file.
     */
    public static ObjectInputStream getObjectInputStream(String filename) throws FileNotFoundException, IOException {
        return getObjectInputStream(new File(filename));
    }

    /**
     * Makes a ObjectInputStream wrapped around the file. Figures out if the stream is zipped using the magic bytes.
     * @param file The input File.
     * @return A ObjectInputStream wrapped around the appropriate stream.
     * @throws FileNotFoundException If the file can't be read.
     * @throws IOException If an error occurred when opening the file.
     */
    public static ObjectInputStream getObjectInputStream(File file) throws FileNotFoundException, IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(file),BUFFER_SIZE);
        return new ObjectInputStream(wrapGZIPStream(stream));
    }

    /**
     * Checks to see if the path is a classpath resource first, before checking the filesystem.
     * @param path The path to open.
     * @return A buffered input stream.
     * @throws FileNotFoundException If the file isn't found.
     */
    public static BufferedInputStream getInputStream(String path) throws FileNotFoundException {
        InputStream in = IOUtil.class.getResourceAsStream(path);
        if (in == null) {
            File file = new File(path);
            in = new FileInputStream(file);
        }
        return new BufferedInputStream(in, BUFFER_SIZE);
    }

    public static BufferedInputStream getInputStream(File file) throws FileNotFoundException {
        InputStream in = new FileInputStream(file);
        return new BufferedInputStream(in, BUFFER_SIZE);
    }

    public static String toString(String path) throws IOException {
        return toString(path, StandardCharsets.UTF_8);
    }

    public static String toString(String path, Charset charset) throws IOException {
        String str = fromResource(path, charset);
        if (str != null) {
            return str;
        } else {
            str = fromFile(path, charset);
            if (str != null) {
                return str;
            } else {
                throw new RuntimeException("Failed to read path " + path);
            }
        }
    }

    public static String fromResource(String path, Charset charset) {
        return fromInputStream(IOUtil.class.getResourceAsStream(path), charset);
    }

    public static String fromPath(Path path) throws FileNotFoundException {
        return fromFile(path.toFile(), StandardCharsets.UTF_8);
    }

    public static String fromPath(Path path, Charset charset) throws FileNotFoundException {
        return fromFile(path.toFile(), charset);
    }

    public static String fromFile(String path, Charset charset) throws FileNotFoundException {
        return fromFile(new File(path), charset);
    }

    public static String fromFile(File file, Charset charset) throws FileNotFoundException {
        if (file.length() == 0) {
            return "";
        }
        return fromInputStream(new FileInputStream(file), charset);
    }

    /**
     * Reads the location specified by a URI into a String. Checks to see if the URI is
     * remote first, and throws IllegalArgumentException if it is.
     * @param uri The URI to read.
     * @param charset The charset to use.
     * @return The String contents of the URI.
     * @throws IOException If the URI failed to load or if it wasn't convertible to a URL.
     */
    public static String fromUri(URI uri, Charset charset) throws IOException {
        return fromUrl(uri.toURL(),charset);
    }

    /**
     * Reads the location specified by a URL into a String. Checks to see if the URL is
     * remote first, and throws IllegalArgumentException if it is.
     * @param url The URL to read.
     * @param charset The charset to use.
     * @return The String contents of the URL.
     * @throws IOException If the URL failed to load.
     */
    public static String fromUrl(URL url, Charset charset) throws IOException {
        if (isDisallowedProtocol(url)) {
            throw new IllegalArgumentException("Tried to read disallowed URL protocol: '" + url.toString() + "'");
        }
        return fromInputStream(url.openStream(),charset);
    }

    private static String fromInputStream(InputStream in, Charset charset) {
        try (Scanner scanner = new Scanner(new BufferedInputStream(in,BUFFER_SIZE),charset.name())) {
            return scanner.useDelimiter("\\Z").next();
        }
    }

    public static <T extends Serializable> void serialize(T object, String path) throws FileNotFoundException, IOException {
        serialize(object, path, BUFFER_SIZE);
    }

    public static <T extends Serializable> void serialize(T object, String path, int bufferSize) throws FileNotFoundException, IOException {
        File file = new File(path);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path), bufferSize))) {
            oos.writeObject(object);
        }
    }

    /**
     * Deserializes an object from the supplied path.
     * Returns {@link Optional#empty} if it failed to cast the type to the required class.
     * @param path The path to read from.
     * @param clazz The class to cast to.
     * @param <T> The type of the serialized object.
     * @return The deserialized instance.
     * @throws IOException If the stream could not be read.
     * @throws ClassNotFoundException If the class isn't available on the classpath.
     */
    public static <T extends Serializable> Optional<T> deserialize(String path, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (InputStream is = getInputStream(path)) {
            return deserialize(is,clazz);
        }
    }

    /**
     * Deserializes an object from the supplied stream.
     * Returns {@link Optional#empty} if it to failed cast the type to the required class.
     * @param path The file to read from.
     * @param clazz The class to cast to.
     * @param <T> The type of the serialized object.
     * @return The deserialized instance.
     * @throws IOException If the stream could not be read.
     * @throws ClassNotFoundException If the class isn't available on the classpath.
     */
    public static <T extends Serializable> Optional<T> deserialize(File path, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (InputStream is = getInputStream(path)) {
            return deserialize(is,clazz);
        }
    }

    /**
     * Deserializes an object from the supplied stream.
     * Returns {@link Optional#empty} if it to failed cast the type to the required class.
     * @param stream The input stream to read from.
     * @param clazz The class to cast to.
     * @param <T> The type of the serialized object.
     * @return The deserialized instance.
     * @throws IOException If the stream could not be read.
     * @throws ClassNotFoundException If the class isn't available on the classpath.
     */
    public static <T extends Serializable> Optional<T> deserialize(InputStream stream, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(stream)) {
            Object obj = ois.readObject();
            if (clazz.isInstance(obj)) {
                return Optional.of(clazz.cast(obj));
            } else {
                logger.warning("Invalid class found, expected " + clazz.getName() + " found " + obj.getClass().getName());
                return Optional.empty();
            }
        }
    }

    public static PrintStream getPrintStream(String path) throws FileNotFoundException {
        return getPrintStream(path, BUFFER_SIZE);
    }

    public static PrintStream getPrintStream(String path, int bufferSize) throws FileNotFoundException {
        return getPrintStream(new File(path),bufferSize);
    }

    public static PrintStream getPrintStream(File file, int bufferSize) throws FileNotFoundException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
        return new PrintStream(bos, false, StandardCharsets.UTF_8);
    }

    public static OutputStream getOutputStream(String path) throws FileNotFoundException {
        return getOutputStream(path, BUFFER_SIZE);
    }

    public static OutputStream getOutputStream(String path, int bufferSize) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(path), bufferSize);
    }

    public static Iterator<Path> getPaths(Path listPath, Path rootPath) {
        try {
            List<String> lines = getLines(listPath.toString());
            List<Path> paths = new ArrayList<>();
            for (String line : lines) {
                Path path = Paths.get(rootPath.toString(), line);
                paths.add(path);
            }
            return paths.iterator();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Iterator<Path> getPaths(String path, String suffix) {
        return getPaths(path, new String[] { suffix });
    }

    public static Iterator<Path> getPaths(String path, String[] suffixes) {
        return getPaths(Paths.get(path), suffixes);
    }

    public static Iterator<Path> getPaths(Path path, String[] suffixes) {
        try {
            Predicate<Path> filter;

            if (suffixes != null && suffixes.length > 0) {
                filter = p -> {
                    String pathName = p.toString();
                    for (String suffix : suffixes) {
                        if (pathName.endsWith(suffix)) {
                            return true;
                        }
                    }
                    return false;
                };
            } else {
                filter = p -> true;
            }
            return Files.walk(path).filter(filter).iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Iterator<Path> getPaths(List<String> fileNames, Path parentPath) {
        return new NamesPathIterator(fileNames.iterator(), parentPath);
    }

    /**
     * Gets an input stream for a given location. We can use the stream
     * to deserialize objects that are part of our configuration.
     * <p>
     * We'll try to use the location as a resource, and failing that a URL, and
     * failing that, a file.
     * <p>
     * If the resource that is to be opened as an InputStream appears to be
     * a gzip file (based on its magic number), a GZipInputStream will
     * automatically be wrapped in to decompress the stream.
     * 
     * @param location the location provided.
     * @return an input stream for that location, or null if we couldn't find
     * any.
     */
    public static InputStream getInputStreamForLocation(String location) {
        URL url = getURLForLocation(location);
        if (url != null) {
            if (isDisallowedProtocol(url)) {
                logger.severe("Tried to open a disallowed URL protocol: " + url.toString());
                return null;
            }
            try {
                InputStream ret = url.openStream();

                //
                // If the stream opened check for a magic number at
                // the beginning of it and wrap the stream in gzip if we need it.
                if (ret != null) {
                    try {
                        return wrapGZIPStream(ret);
                    } catch (IOException e) {
                        logger.log(Level.WARNING,"Failed to check stream for GZIP bytes",e);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.FINER,"Failed to open location " + location);
            }
        }
        // else we failed, return null;
        return null;
    }

    /**
     * Checks if the stream contains a GZIP input stream, if so wrap it in the appropriate
     * decoder and return.
     * @param input An input stream.
     * @return Either a {@link PushbackInputStream} wrapped round the input, or a {@link GZIPInputStream}
     *         wrapped round the pushback stream wrapped around the input.
     * @throws IOException If the stream could not be read.
     */
    private static InputStream wrapGZIPStream(InputStream input) throws IOException {
        //
        // A pushback input stream lets us peek at the first couple bytes
        // and put them back after checking the magic number.
        PushbackInputStream pis = new PushbackInputStream(input, 2);
        byte[] magic = new byte[2];
        int len = pis.read(magic);
        if (len > 0) {
            pis.unread(magic, 0, len);
            //
            // Check the bytes we read
            if (magic[0] == (byte) GZIPInputStream.GZIP_MAGIC && magic[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)) {
                //
                // This is GZIP. Wrap the pushback input stream. We can't
                // use the old "ret" because we've already read a couple
                // bytes out of it.
                return new GZIPInputStream(pis);
            } else {
                return pis;
            }
        } else {
            return pis;
        }
    }

    /**
     * Gets a URL for a given location.
     * <p>
     * We'll try to use the location as a resource, and failing that a URL, and
     * failing that, a file.
     * @param location the location provided.
     * @return a URL to that location, or null if we couldn't find
     * any.
     */
    public static URL getURLForLocation(String location) {
        URL ret = null;
        try {
            //
            // First, see if it's a resource on our classpath.
            ret = IOUtil.class.getResource(location);
        } catch (InvalidPathException e) {
            // Swallow exception from invalid path on Windows in an exploded module.
            // Can be removed when JDK-8300228 is fixed.
        }
        if (ret == null) {
            try {
                //
                // Nope. See if it's a valid URL and open that.
                ret = new URL(location);
            } catch (MalformedURLException ex) {
                try {
                    //
                    // Not a valid URL, so try it as a file name.
                    ret = new File(location).toURI().toURL();
                } catch (MalformedURLException ex1) {
                    //
                    // Couldn't open the file, we're done.
                    logger.warning("Cannot open location " + location);
                    return null;
                }
            }
        }
        return ret;
    }

    /**
     * Checks the url to see if the protocol is disallowed.
     * Disallowed protocols are http, https, ftp. Null
     * URLs are also disallowed.
     * @param url The URL to check.
     * @return True if the protocol is disallowed, the URL is null, or the protocol is null.
     */
    public static boolean isDisallowedProtocol(URL url) {
        if (url == null) {
            return true;
        }
        String protocol = url.getProtocol();
        if (protocol == null) {
            return true;
        }
        return switch (protocol) {
            case "http", "https", "ftp" -> true;
            default -> false;
        };
    }

    public static class NamesPathIterator implements Iterator<Path>{

        private final Iterator<String> fileNames;
        private final Path parentPath;
        
        public NamesPathIterator(Iterator<String> fileNames, Path parentPath) {
            super();
            this.fileNames = fileNames;
            this.parentPath = parentPath;
        }

        @Override
        public boolean hasNext() {
            return fileNames.hasNext();
        }

        @Override
        public Path next() {
            if (hasNext()) {
                return Paths.get(parentPath.toString(), fileNames.next());
            } else {
                throw new NoSuchElementException();
            }
        }
        
    }

    public static Iterator<String> getStringPaths(Iterator<Path> paths) {
        return new StringPathIterator(paths);
    }

    public static class StringPathIterator implements Iterator<String> {
        private final Iterator<Path> paths;

        public StringPathIterator(Iterator<Path> paths) {
            super();
            this.paths = paths;
        }

        @Override
        public boolean hasNext() {
            return paths.hasNext();
        }

        @Override
        public String next() {
            return paths.next().toString();
        }
    }
    
    public static URI createClasspathURI(String resourcePath) throws URISyntaxException {
        return new URI("classpath:" + resourcePath);
    }

    public static String replaceBackSlashes(String path) {
        return path.replaceAll("\\\\", "/");
    }
}
