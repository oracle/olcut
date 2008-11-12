package com.sun.labs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to parse time specs like 1d2h30m2s455ms into the number of milliseconds
 * that they represent.  Only integer times will be taken into account.  This will
 * be parsed using a regular expression, so things like 1d300m are allowed and will be
 * parsed correctly.
 */
public class TimeSpec {

    private static Pattern timepat = Pattern.compile("(?:(\\d+)(d))??(?:(\\d+)(h))??(?:(\\d+)(m))??(?:(\\d+)(s))??(?:(\\d+)(ms))??");

    private static Map<String,TimeUnit> tum;

    static {
        tum = new HashMap<String,TimeUnit>();
        tum.put("d", TimeUnit.DAYS);
        tum.put("h", TimeUnit.HOURS);
        tum.put("m", TimeUnit.MINUTES);
        tum.put("s", TimeUnit.SECONDS);
        tum.put("ms", TimeUnit.MILLISECONDS);
    }

    public static long parse(String timespec) throws IllegalArgumentException {
        Matcher m = timepat.matcher(timespec);
        long nms = 0;

        if(m.matches()) {
            for(int i = 1; i <= m.groupCount(); i += 2) {
                if(m.group(i) != null) {
                    long dur = Integer.parseInt(m.group(i));
                    TimeUnit tu = tum.get(m.group(i+1));
                    nms += tu.toMillis(dur);
                }
            }
        } else {
            throw new IllegalArgumentException("Couldn't parse timespec " + timespec);
        }
        return nms;
    }
}
