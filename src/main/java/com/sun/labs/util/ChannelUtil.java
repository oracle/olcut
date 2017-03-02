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
package com.sun.labs.util;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

public class ChannelUtil {

    private static Logger logger = Logger.getLogger(ChannelUtil.class.getName());

    /**
     * The block size in which buffers will be written.
     */
    protected final static int BLOCK_SIZE = 1 << 16;

    /**
     * Writes a byte buffer fully to the given channel.  The data is
     * written in chunks of BLOCK_SIZE bytes.
     *
     * @param c The channel that we will write to.
     * @param b The buffer we wish to write.
     * @throws java.io.IOException If there is any error during writing.
     */
    public static void writeFully(WritableByteChannel c, ByteBuffer b)
            throws java.io.IOException {

        if(b.isDirect()) {
            writeFullyInternal(c, b);
            return;
        }

        int limit = b.limit();
        if(limit < BLOCK_SIZE) {
            writeFullyInternal(c, b);
            return;
        }

        int nBlocks = limit / BLOCK_SIZE;
        if(limit % BLOCK_SIZE != 0) {
            nBlocks++;
        }

        ByteBuffer wb = ByteBuffer.allocateDirect(BLOCK_SIZE);
        for(int i = 0, pos = 0; i < nBlocks; i++, pos += BLOCK_SIZE) {
            b.position(pos).limit(Math.min(pos + BLOCK_SIZE, limit));
            writeFullyInternal(c, (ByteBuffer) wb.put(b).flip());
            wb.clear();
        }
    }

    /**
     * Writes an array of byte buffers fully to the given channel.  The
     * data is written in chunks of BLOCK_SIZE bytes.
     * @param size The number of bytes to write to the channels.
     * @param c The channel that we will write to.
     * @param b The buffer we wish to write.
     * @throws java.io.IOException If there is any error during writing.
     */
    public static void writeFully(GatheringByteChannel c,
            ByteBuffer[] b, long size)
            throws java.io.IOException {

        if(size == 0) {
            for(int i = 0; i < b.length; i++) {
                size += b[i].remaining();
            }
        }

        long remain = size;
        while(remain > 0) {
            remain -= c.write(b);
        }
    }

    /**
     * Writes a byte buffer fully to the given channel.
     *
     * @param c The channel that we will write to.
     * @param b The buffer we wish to write.
     * @return The number of byte read.
     * @throws java.io.IOException If there is any error during writing.
     */
    protected static int writeFullyInternal(WritableByteChannel c, ByteBuffer b)
            throws java.io.IOException {
        int written = 0;
        while(b.remaining() > 0) {
            written += c.write(b);
        }
        return written;
    }

    /**
     * Reads a byte buffer fully from the given channel, retrying if
     * necessary. 
     *
     * @param c The channel that we will write to.
     * @param b The buffer we wish to write.
     * @return The byte buffer.
     * @throws java.io.IOException If there is any error during reading.
     */
    public static ByteBuffer readFully(ReadableByteChannel c, ByteBuffer b)
            throws java.io.IOException {
        while(b.remaining() > 0) {
            int bytesRead = c.read(b);

            //
            // Check for end-of-stream.
            if(bytesRead == -1) {
                break;
            }
        }
        return b;
    }

    /**
     * Reads a byte buffer fully from the given channel at the given
     * position, retrying if necessary. 
     *
     * @param c The channel that we will write to.
     * @param off The offset to read from.
     * @param b The buffer we wish to write.
     * @return The buffer.
     * @throws java.io.IOException If there is any error during reading.
     */
    public static ByteBuffer readFully(FileChannel c, long off, ByteBuffer b)
            throws java.io.IOException {
        while(b.remaining() > 0) {
            int bytesRead = c.read(b, off);

            //
            // Check for end-of-stream.
            if(bytesRead == -1) {
                break;
            }
            off += bytesRead;
        }
        return b;
    }

    /**
     * Transfers the complete content of a channel to another, making sure
     * that all data is written.
     * @param src The source channel.
     * @param dst The destination channel.
     * @throws java.io.IOException If there is any error transferring the data.
     */
    public static void transferFully(FileChannel src,
            FileChannel dst)
            throws java.io.IOException {

        long written = 0;
        long size = src.size();
        while(written < size) {
            written += src.transferTo(written,
                    size - written,
                    dst);
        }
    }

    /**
     * Transfers a portion of the content of one channel to another, making
     * sure that all data is written.
     * @param src The source channel.
     * @param position The position in the source channel.
     * @param count The number of bytes to transfer.
     * @param dst The destination channel.
     * @throws java.io.IOException If there is any error transferring the data.
     */
    public static void transferFully(FileChannel src,
            long position,
            long count,
            FileChannel dst)
            throws java.io.IOException {
        while(count > 0) {
            long n = src.transferTo(position, count, dst);
            count -= n;
            position += n;
        }
    }
} // ChannelUtil
