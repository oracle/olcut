/*
 * Copyright 1999-2002 Carnegie Mellon University.
 * Portions Copyright 2002 Sun Microsystems, Inc.
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package com.oracle.labs.mlrg.olcut.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

/**
 * Utilities for files.
 */
public class FileUtil {

    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    /**
     * Recursively deletes a directory and everything in it.
     * @param dir the directory to delete.
     */
    public static void deleteDirectory(File dir) {
        if(!dir.isDirectory()) {
            logger.warning(String.format("%s is not a directory, not deleting", dir));
            return;
        }
        if(!dir.exists()) {
            return;
        }
        File[] fs = dir.listFiles();
        for(File f : fs) {
            if(f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }

    public static void dirCopier(File source, File target) throws IOException {
        if(!source.isDirectory()) {
            throw new IOException(source + " is not a directory");
        }
        if(!target.exists()) {
            if(!target.mkdirs()) {
                throw new IOException("Failed to create " + target.getName());
            }
        }
        if(!target.isDirectory()) {
            throw new IOException(target + " is not a directory");
        }
        copyDir(source, target);
    }
    
    private static void copyDir(File sd, File td) throws java.io.IOException {
        File[] files = sd.listFiles();
        for(File f : files) {
            File nt = new File(td, f.getName());
            if(f.isDirectory()) {
                if(!nt.mkdir()) {
                    throw new IOException("Failed to make dir " + nt.getName());
                }
                copyDir(f, nt);
            } else {
                copyFile(f, nt);
            }
        }
    }

    private static void copyFile(File sf, File tf) throws java.io.IOException {
        RandomAccessFile sr = new RandomAccessFile(sf, "r");
        RandomAccessFile tr = new RandomAccessFile(tf, "rw");
        ChannelUtil.transferFully(sr.getChannel(), tr.getChannel());
        sr.close();
        tr.close();
    }
}
