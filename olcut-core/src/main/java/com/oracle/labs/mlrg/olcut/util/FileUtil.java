/*
 * Copyright 1999-2002 Carnegie Mellon University.
 * Portions Copyright 2002 Sun Microsystems, Inc.
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
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
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    if (!f.delete()) {
                        logger.log(Level.INFO,"Failed to delete file: " + f.getName());
                    }
                }
            }
        }
        if (!dir.delete()) {
            logger.log(Level.INFO, "Failed to delete directory: " + dir.getName());
        }
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
        if (files != null) {
            for (File f : files) {
                File nt = new File(td, f.getName());
                if (f.isDirectory()) {
                    if (!nt.mkdir()) {
                        throw new IOException("Failed to make dir " + nt.getName());
                    }
                    copyDir(f, nt);
                } else {
                    copyFile(f, nt);
                }
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
