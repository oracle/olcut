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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides a log formatter for use with sphinx. This formatter generates nicer looking console messages than the
 * default formatter. To use the formatter, set the property
 * <p/>
 * java.util.logging.ConsoleHandler.formatter to edu.cmu.sphinx.util.LabsLogFormatter
 * <p/>
 * This is typically done in a custom logger.properties file
 */
public class SimpleLabsLogFormatter extends Formatter {

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
    public String format(LogRecord record) {
        String message = formatMessage(record);
        if(terse) {
            return message + "\n";
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
                msg = msg + "\n" + sw.toString();
            }
            return msg + "\n";
        }
    }

}
