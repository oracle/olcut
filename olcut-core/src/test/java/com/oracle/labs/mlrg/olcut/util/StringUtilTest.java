package com.oracle.labs.mlrg.olcut.util;

import static org.junit.Assert.*;

import org.junit.Test;

import static com.oracle.labs.mlrg.olcut.util.StringUtil.normalize;
public class StringUtilTest {

	@Test
	public void testNormalize() throws Exception {
		assertEquals("kokyokyoku", normalize("kōkyōkyoku"));
		assertEquals("dziekanski", normalize("dziekański"));
		assertEquals("melantois", normalize("mélantois"));
	}
}
