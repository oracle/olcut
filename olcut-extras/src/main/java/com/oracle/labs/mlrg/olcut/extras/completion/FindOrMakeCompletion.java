/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.extras.completion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class FindOrMakeCompletion {
    private static final Logger logger = Logger.getLogger(FindOrMakeCompletion.class.getName());

    private static final String OLCUT_DIR = "OLCUT_HOME";

    private static String checksum(String filePath) {
        Adler32 checksum = new Adler32();
        try(BufferedInputStream is = new BufferedInputStream(new CheckedInputStream(Files.newInputStream(Paths.get(filePath)), checksum))) {
            byte[] buf = new byte[8192];
            int len;
            // we just want to read the whole stream for the side-effect of the checksum
            while((len = is.read(buf)) != -1) {}
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read file :" + filePath, e);
            System.exit(1);
        }
        return Long.toString(checksum.getValue());
    }

    public static void main(String[] args) {
        String jarId = checksum(args[0]);

        // N.B. This is a duplicate of Util.getOlcutRoot in olcut-core, because that's the only thing we need from there
        Path olcutRoot = Optional.ofNullable(System.getenv(OLCUT_DIR)).map(Paths::get).orElse(Paths.get(System.getProperty("user.home")).resolve(".olcut"));

        Path completionDir = olcutRoot.resolve("completions");
        Path targetJar = completionDir.resolve(jarId + ".completion");
        if(!Files.exists(targetJar)) {
            //logger.info("No completion for " + args[0] + "with " + HASH_ALGO + " of " + jarId + " found in " + completionDir.toAbsolutePath().toString() + ". generating");
            try {
                Files.createDirectories(targetJar.getParent());
                Files.write(targetJar, (Iterable<String>) GenCompletion.completions(new File(args[0]))
                        .map(ClassCompletion::completionString)::iterator);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error writing to " + targetJar.toAbsolutePath().toString(), e);
                System.exit(1);
            }
        }
        System.out.println(targetJar.toAbsolutePath().toString());
    }
}
