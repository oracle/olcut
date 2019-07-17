package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a file (also used for a Path).
 *
 * Converts each input into a path, gets the absolute path, normalizes it then stores the file.
 */
public final class FileProvenance implements PrimitiveProvenance<File> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final File value;

    public FileProvenance(String key, File value) {
        this.key = key;
        this.value = value.toPath().toAbsolutePath().normalize().toFile();
    }

    public FileProvenance(String key, Path value) {
        this.key = key;
        this.value = value.toAbsolutePath().normalize().toFile();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public File getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileProvenance)) return false;
        FileProvenance that = (FileProvenance) o;
        return key.equals(that.key) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
