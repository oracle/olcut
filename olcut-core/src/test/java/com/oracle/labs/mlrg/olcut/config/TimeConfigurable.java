package com.oracle.labs.mlrg.olcut.config;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Objects;

/**
 *
 */
public final class TimeConfigurable implements Configurable {

    @Config(mandatory=true)
    public LocalDate date;

    @Config(mandatory=true)
    public OffsetDateTime dateTime;

    @Config(mandatory=true)
    public OffsetTime time;

    private TimeConfigurable() {}

    public TimeConfigurable(LocalDate date, OffsetDateTime dateTime, OffsetTime time) {
        this.date = date;
        this.dateTime = dateTime;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeConfigurable)) return false;
        TimeConfigurable that = (TimeConfigurable) o;
        return date.equals(that.date) &&
                dateTime.equals(that.dateTime) &&
                time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, dateTime, time);
    }
}
