/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for time specifications.
 */
public class TimeSpecTest {

    public TimeSpecTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSingle() {
        long x = TimeSpec.parse("1s");
        assertTrue(x == TimeUnit.SECONDS.toMillis(1));
        x = TimeSpec.parse("10s");
        assertTrue(x == TimeUnit.SECONDS.toMillis(10));
        x = TimeSpec.parse("1m");
        assertTrue(x == TimeUnit.MINUTES.toMillis(1));
        x = TimeSpec.parse("1h");
        assertTrue(x == TimeUnit.HOURS.toMillis(1));
    }

    @Test
    public void testTwo() {
        long x = TimeSpec.parse("1m1s");
        assertEquals(TimeUnit.MINUTES.toMillis(1) + TimeUnit.SECONDS.toMillis(1), x);
        x = TimeSpec.parse("1h1m");
        assertEquals(TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(1), x);
        x = TimeSpec.parse("1d1h");
        assertEquals(TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1), x);
    }

    @Test
    public void testThree() {
        long x = TimeSpec.parse("2d5h10m");
        assertEquals(TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(10), x);
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void badOrder() {
        long x = TimeSpec.parse("2h5d");
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void badUnit() {
        long x = TimeSpec.parse("2h5ns");
    }
}