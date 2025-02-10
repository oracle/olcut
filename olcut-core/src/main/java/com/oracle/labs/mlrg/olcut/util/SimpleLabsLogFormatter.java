/*
 * Copyright (c) 2004, 2025, Oracle and/or its affiliates.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Provides a log formatter. This formatter generates nicer looking console messages than the
 * default formatter. To use the formatter, set the property
 *
 * <code>java.util.logging.ConsoleHandler.formatter</code> to <code>com.oracle.labs.mlrg.olcut.util.SimpleLabsLogFormatter</code>
 *
 * This is typically done in a custom logger.properties file
 */
public class SimpleLabsLogFormatter extends Formatter {
    private static final Logger logger = Logger.getLogger(SimpleLabsLogFormatter.class.getName());

    private boolean terse;

    /**
     * Sets the level of output
     *
     * @param terse if true, the output level should be terse
     */
    public void setTerse(boolean terse) {
        this.terse = terse;
    }

    /**
     * Retrieves the level of output
     *
     * @return the level of output
     */
    public boolean getTerse() {
        return terse;
    }

    /**
     * Formats the given log record and return the formatted string.
     *
     * @param record the record to format
     * @return the formatted string
     */
    @Override
    public String format(LogRecord record) {
        String message = formatMessage(record);
        if(terse) {
            return message + '\n';
        } else {
            String msg = String.format("[%tD %tT:%tL] %s %s",
                                       record.getMillis(), 
                                       record.getMillis(), 
                                       record.getMillis(),
                                       record.getLevel(),
                                       message);
            if(record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                msg = msg + '\n' + sw.toString();
            }
            return msg + '\n';
        }
    }

    public static void setAllLogFormatters() {
        setAllLogFormatters(Level.ALL);
    }

    public static void setAllLogFormatters(Level level) {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(level);
            h.setFormatter(new SimpleLabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch (Exception ex) {
                logger.severe("Error setting output encoding");
            }
        }
    }

}
