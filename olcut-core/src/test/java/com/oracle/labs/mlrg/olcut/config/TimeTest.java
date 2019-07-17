package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class TimeTest {

    @Test
    public void testValid() {
        ConfigurationManager cm = new ConfigurationManager("timeConfig.xml");
        TimeConfigurable time = (TimeConfigurable) cm.lookup("valid-time");
        assertEquals(OffsetTime.parse("12:34+00:00"),time.time);
        assertEquals(LocalDate.parse("1066-10-04"),time.date);
        assertEquals(OffsetDateTime.parse("1949-06-16T20:30:00+01:00"),time.dateTime);
    }

    @Test
    public void testInvalidTime() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-time");
        });
    }

    @Test
    public void testInvalidDate() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-date");
        });
    }

    @Test
    public void testInvalidDateTime() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-date-time");
        });
    }

}
