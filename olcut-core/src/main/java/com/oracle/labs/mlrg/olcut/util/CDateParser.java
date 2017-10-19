/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package com.oracle.labs.mlrg.olcut.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 */
public class CDateParser {

    static long _32_bit_seconds = 0x7fffffffL * 1000L;	// largest date that fits in 32 bits

    LinkedList<DateFormat> dateFormats = new LinkedList<>();

    static String[] USFormatStrings = {
        "E, d MMM y h:m:s.S a z",
        "yyyy-MM-dd'T'H:m:s'Z'",
        "E, d MMM y h:m:s a z",
        "E, d MMM y h:m:s a",
        "E, d MMM y H:m:s z",
        "E, d MMM y H:m:s",
        "E, d MMM y z",
        "E, d MMM y",
        "E, MMM d y h:m:s a z",
        "E, MMM d y h:m:s a",
        "E, MMM d y H:m:s z",
        "E, MMM d y H:m:s",
        "E, MMM d y z",
        "E, MMM d y",
        "M/d/y h:m:s a z",
        "M/d/y h:m:s a",
        "M/d/y H:m:s z",
        "M/d/y H:m:s",
        "M/d/y z",
        "M/d/y",
        "M-d-y h:m:s a z",
        "M-d-y h:m:s a",
        "M-d-y H:m:s z",
        "M-d-y H:m:s",
        "M-d-y z",
        "M-d-y",
        "M.d.y h:m:s a z",
        "M.d.y h:m:s a",
        "M.d.y H:m:s z",
        "M.d.y H:m:s",
        "M.d.y z",
        "M.d.y",
        "MMM d y h:m:s a z",
        "MMM d y h:m:s a",
        "MMM d y H:m:s z",
        "MMM d y H:m:s",
        "MMM d y z",
        "MMM d y",
        "d MMM y h:m:s a z",
        "d MMM y h:m:s a",
        "d MMM y H:m:s z",
        "d MMM y H:m:s",
        "d MMM y z",
        "d MMM y",
        "E MMM d h:m:s a z y",
        "E MMM d H:m:s z y",
        "EEE, MMM d, ''yy",
        "h:mm a",
        "H:mm",
        "hh 'o''clock' a, zzzz",
        "yyyyy.MMMMM.dd GGG hh:mm aaa",
        "yyyyy.MMMMM.dd GGG HH:mm",
        "E,MMM d y z",
        "E,MMM d y",
        "E,d MMM y h:m:s a z",
        "E,d MMM yyyy h:m:s a z",
        "E,d MMM y h:m:s a",
        "E,d MMM yyyy h:m:s a",
        "E,d MMM y H:m:s z",
        "E,d MMM yyyy H:m:s z",
        "E,d MMM y H:m:s",
        "E,d MMM yyyy H:m:s",
        "E,d MMM y z",
        "E,d MMM yyyy z",
        "E,d MMM y",
        "E,d MMM yyyy",
        "E, d-MMM-y h:m:s a z",
        "E, d-MMM-y h:m:s a",
        "E, d-MMM-y H:m:s z",
        "E, d-MMM-y H:m:s",
        "E, d-MMM-y z",
        "E, d-MMM-y",
        "E MMM d h:m:s y a z",
        "E MMM d h:m:s y a",
        "E MMM d H:m:s y z",
        "E MMM d H:m:s y",
        "E MMM d y z",
        "E MMM d y",
        "dd-MMM-yyyy HH:mm:ss.SS",
        "d-MMM-y h:m:s a z",
        "d-MMM-y h:m:s a",
        "d-MMM-y H:m:s z",
        "d-MMM-y H:m:s",
        "d-MMM-y z",
        "d-MMM-y",
        "y-M-d",
        "M d y",
        "y M d",
        "MMM y",};

    public CDateParser() {
        // Add US formats
        for(int i = 0; i < USFormatStrings.length; ++i) {
            dateFormats.add(new SimpleDateFormat(USFormatStrings[i], Locale.US));
        }
        // Add local formats
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                       DateFormat.FULL));
        dateFormats.add(DateFormat.getDateInstance(DateFormat.FULL));
        dateFormats.add(DateFormat.getTimeInstance(DateFormat.FULL));
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                       DateFormat.LONG));
        dateFormats.add(DateFormat.getDateInstance(DateFormat.LONG));
        dateFormats.add(DateFormat.getTimeInstance(DateFormat.LONG));
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                       DateFormat.MEDIUM));
        dateFormats.add(DateFormat.getDateInstance(DateFormat.MEDIUM));
        dateFormats.add(DateFormat.getTimeInstance(DateFormat.MEDIUM));
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                       DateFormat.SHORT));
        dateFormats.add(DateFormat.getDateInstance(DateFormat.SHORT));
        dateFormats.add(DateFormat.getTimeInstance(DateFormat.SHORT));
        // XXX Should have a way to register more formats from config file
    }

    /**
     * This date parser understands many US and locale based formats. Typical RD
     * dates look like: Fri, 17 Jan 1997 16:42:49 GMT (this format derives from
     * http timestamps)
     * @param date The input date as a String.
     * @return A parsed Date object.
     * @throws ParseException If it failed to parse a date.
     */
    public Date parse(String date) throws ParseException {
        Date d = null;
        for(Iterator<DateFormat> i = dateFormats.iterator(); i.hasNext();) {
            try {
                DateFormat df = i.next();
                d = df.parse(date);
                return d;
            } catch(Exception e) {
            }
        }

        if(d == null) {
            // nothing parsed - try some special cases
            try {
                if(date.equals("0")
                        || date.equalsIgnoreCase("now")
                        || date.equalsIgnoreCase("today")
                        || date.equalsIgnoreCase("immediately")
                        || date.equalsIgnoreCase("expired")) {
                    return new Date();
                } else if(date.equalsIgnoreCase("never")) {
                    return new Date(Long.MAX_VALUE);
                } else {
                    // [+]/-n days
                    if(date.startsWith("+")) {
                        date = date.substring(1);
                    }
                    long i = Long.parseLong(date);
                    return new Date(new Date().getTime() + i * 24 * 60 * 60
                            * 1000);
                }
            } catch(Exception ignored) {
            }
        }

        throw new ParseException("Date failed to parse", 0);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String d = null;
        CDateParser dp = new CDateParser();
        while((d = r.readLine()) != null) {

            try {
                Date date = dp.parse(d);
                System.out.format("Parsed: %s\n", date);
            } catch(ParseException ex) {
                System.out.format("%s failed to parse\n", d);
            }
        }
    }
}
