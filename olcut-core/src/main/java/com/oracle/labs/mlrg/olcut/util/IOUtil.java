package com.oracle.labs.mlrg.olcut.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class IOUtil {
    private static final Logger logger = Logger.getLogger(IOUtil.class.getName());
    public static final int BUFFER_SIZE = 1000000;

    public static List<String> getLinesFromString(String text) {
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static List<String> getLines(String path) {
        return getLines(path, -1);
    }

    public static List<String> getLines(String path, String encoding) {
        return getLines(path, -1, encoding);
    }

    public static List<String> getLines(String path, int count) {
        return getLines(path, count, "UTF-8");
    }

    public static List<String> getLines(String path, int count, String encoding) {
        try (BufferedReader reader = getReader(path, encoding)) {
            return getLines(reader,count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getLines(BufferedReader reader, int count) {
        try {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (count > 0 && lines.size() == count) {
                    return lines;
                }
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader getReader(String path) {
        return getReader(path, "UTF-8");
    }

    public static BufferedReader getReader(String path, String charSet) {
        try {
            return new BufferedReader(new InputStreamReader(getInputStream(path), charSet),BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader getReader(URI uri, String charSet) {
        try {
            InputStream is = uri.toURL().openStream();
            return new BufferedReader(new InputStreamReader(is, charSet),BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader getReader(Path path, String charSet) {
        try {
            InputStream is = new FileInputStream(path.toFile());
            return new BufferedReader(new InputStreamReader(is, charSet),BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes a reader wrapped around the string. Either zipped or not.
     * @param filename The input filename.
     * @param zipped Is the file zipped?
     * @return A BufferedReader wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static BufferedReader getReader(String filename, String charSet, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        return getReader(new File(filename), charSet, zipped);
    }

    /**
     * Makes a reader wrapped around the string. Either zipped or not.
     * @param zipped Is the file zipped?
     * @return A BufferedReader wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static BufferedReader getReader(File file, String charSet, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        BufferedReader fileReader;
        if (zipped) {
            fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), charSet),BUFFER_SIZE);
        } else {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet),BUFFER_SIZE);
        }
        return fileReader;
    }


    /**
     * Makes a writer wrapped around the string. Either zipped or not.
     * @param filename The output filename.
     * @param zipped Is the file zipped?
     * @return A PrintWriter wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static PrintWriter getPrintWriter(String filename, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        PrintWriter fileWriter;
        if (zipped) {
            fileWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename)), StandardCharsets.UTF_8),BUFFER_SIZE));
        } else {
            fileWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8),BUFFER_SIZE));
        }
        return fileWriter;
    }

    /**
     * Makes a writer wrapped around the string. Either zipped or not.
     * @param filename The output filename.
     * @param zipped Is the file zipped?
     * @return A PrintWriter wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static ObjectOutputStream getObjectOutputStream(String filename, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        ObjectOutputStream objectWriter;
        if (zipped) {
            objectWriter = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE)));
        } else {
            objectWriter = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE));
        }
        return objectWriter;
    }

    /**
     * Makes a ObjectInputStream wrapped around the string. Either zipped or not.
     * @param filename The input filename.
     * @param zipped Is the file zipped?
     * @return A ObjectInputStream wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static ObjectInputStream getObjectInputStream(String filename, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        return getObjectInputStream(new File(filename),zipped);
    }

    /**
     * Makes a ObjectInputStream wrapped around the string. Either zipped or not.
     * @param file The input File.
     * @param zipped Is the file zipped?
     * @return A ObjectInputStream wrapped around the appropriate stream.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static ObjectInputStream getObjectInputStream(File file, boolean zipped) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        ObjectInputStream objectReader;
        if (zipped) {
            objectReader = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file),BUFFER_SIZE)));
        } else {
            objectReader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file),BUFFER_SIZE));
        }
        return objectReader;
    }

    public static InputStream getInputStream(String path) {
        try {
            InputStream in = IOUtil.class.getResourceAsStream(path);
            if (in == null) {
                File file = new File(path);
                in = new FileInputStream(file);
            }
            return new BufferedInputStream(in, BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getInputStream(File file) {
        try {
            InputStream in = new FileInputStream(file);
            return new BufferedInputStream(in, BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(String path) {
        return toString(path, "UTF-8");
    }

    public static String toString(String path, String charSet) {
        String str = null;
        try {
            str = fromResource(path, charSet);
            if (str != null) {
                return str;
            }
        } catch (Exception e) {
        }

        str = fromFile(path, charSet);
        if (str != null) {
            return str;
        }

        throw new RuntimeException("contents not readable: " + path);
    }

    public static String fromResource(String path, String charSet) {
        return fromInputStream(IOUtil.class.getResourceAsStream(path), charSet);
    }

    public static String fromPath(Path path) {
        return fromFile(path.toFile(), "UTF-8");
    }

    public static String fromPath(Path path, String charSet) {
        return fromFile(path.toFile(), charSet);
    }

    public static String fromFile(String path, String charSet) {
        return fromFile(new File(path), charSet);
    }

    public static String fromFile(File file, String charSet) {
        try {
            if (file.length() == 0) {
                return "";
            }
            return fromInputStream(new FileInputStream(file), charSet);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromUri(URI uri, String charSet) {
        try {
            return fromInputStream(uri.toURL().openStream(), charSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fromInputStream(InputStream in, String charSet) {
        try (Scanner scanner = new Scanner(new BufferedInputStream(in,BUFFER_SIZE),charSet)) {
            return scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Serializable> void serialize(T object, String path) {
        serialize(object, path, BUFFER_SIZE);
    }

    public static <T extends Serializable> void serialize(T object, String path, int bufferSize) {
        try {
            File file = new File(path);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path), bufferSize));
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Serializable> T deserialize(String path) {
        return deserialize(getInputStream(path));
    }

    public static <T extends Serializable> T deserialize(File path) {
        return deserialize(getInputStream(path));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(InputStream stream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(stream);
            T object = (T) ois.readObject();
            ois.close();
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PrintStream getPrintStream(String path) {
        return getPrintStream(path, BUFFER_SIZE);
    }

    public static PrintStream getPrintStream(String path, int bufferSize) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path), bufferSize);
            return new PrintStream(bos, false);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        }
    }

    public static PrintStream getPrintStream(File file, int bufferSize) {
        try {
            file.getParentFile().mkdirs();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
            return new PrintStream(bos, false);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        }
    }

    public static OutputStream getOutputStream(String path) {
        return getOutputStream(path, BUFFER_SIZE);
    }

    public static OutputStream getOutputStream(String path, int bufferSize) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path), bufferSize);
            return bos;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Iterator<Path> getPaths(Path listPath, Path rootPath) {
        List<String> lines = getLines(listPath.toString());
        List<Path> paths = new ArrayList<>();
        for (String line : lines) {
            Path path = Paths.get(rootPath.toString(), line);
            paths.add(path);
        }
        return paths.iterator();
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
     * <P>
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
        //
        // First, see if it's a resource on our classpath.
        InputStream ret = IOUtil.class.getResourceAsStream(location);
        if (ret == null) {
            try {
                //
                // Nope. See if it's a valid URL and open that.
                URL sfu = new URL(location);
                ret = sfu.openStream();
            } catch (MalformedURLException ex) {
                try {
                    //
                    // Not a valid URL, so try it as a file name.
                    ret = new FileInputStream(location);
                } catch (FileNotFoundException ex1) {
                    //
                    // Couldn't open the file, we're done.
                    return null;
                }
            } catch (IOException ex) {
                //
                // No joy.
                logger.warning("Cannot open serialized form " + location);
                return null;
            }
        }
        
        //
        // If we have an input stream here, check for a magic number at
        // the beginning of the file and wrap it in gzip if we need it.
        if (ret != null) {
            //
            // A pushback input stream lets us peek at the first couple bytes
            // and put them back after checking the magic number.
            PushbackInputStream pis = new PushbackInputStream(ret, 2);
            try {
                byte[] magic = new byte[2];
                int len = pis.read(magic);
                if (len > 0) {
                    pis.unread(magic, 0, len);
                    //
                    // Check the bytes we read
                    if (magic[0] == (byte)GZIPInputStream.GZIP_MAGIC && magic[1] == (byte)(GZIPInputStream.GZIP_MAGIC >> 8)) {
                        //
                        // This is GZIP. Wrap the pushback input stream. We can't
                        // use the old "ret" because we've already read a couple
                        // bytes out of it.
                        ret = new GZIPInputStream(pis);
                    } else {
                        ret = pis;
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error while reading input stream for gzip." +
                        " Position in stream uncertain.", e);
                ret = pis;                
            }
        }
        return ret;
    }

    /**
     * Gets a URL for a given location.
     * <P>
     * We'll try to use the location as a resource, and failing that a URL, and
     * failing that, a file.
     * @param location the location provided.
     * @return a URL to that location, or null if we couldn't find
     * any.
     */
    public static URL getURLForLocation(String location) {
        //
        // First, see if it's a resource on our classpath.
        URL ret = IOUtil.class.getResource(location);
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

    public static class NamesPathIterator implements Iterator<Path>{

        private Iterator<String> fileNames;
        private Path parentPath;
        
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
            if(hasNext()) {
                return Paths.get(parentPath.toString(), fileNames.next());
            }
            throw new NoSuchElementException();
        }
        
    }

    public static Iterator<String> getStringPaths(Iterator<Path> paths) {
        return new StringPathIterator(paths);
    }

    public static class StringPathIterator implements Iterator<String> {
        private Iterator<Path> paths;

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
    
    public static URI createClasspathURI(String resourcePath) {
        try {
            return new URI("classpath:" + resourcePath);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String replaceBackSlashes(String path) {
    	return path.replaceAll("\\\\", "/");
    }
}
