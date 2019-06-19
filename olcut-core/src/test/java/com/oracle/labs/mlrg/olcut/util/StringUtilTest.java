package com.oracle.labs.mlrg.olcut.util;


import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.StringUtil.normalize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

	@Test
	public void testNormalize() throws Exception {
		assertEquals("kokyokyoku", normalize("kōkyōkyoku"));
		assertEquals("dziekanski", normalize("dziekański"));
		assertEquals("melantois", normalize("mélantois"));
	}
}
