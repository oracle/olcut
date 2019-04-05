package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class TypedProcessorsTest {

	@Test
	public void testStringType() throws Exception {
		File configFile = new File("src/test/resources/com/oracle/labs/mlrg/olcut/config/typedProcessorsConfig.xml");
		ConfigurationManager cm = new ConfigurationManager(configFile.toURI().toURL());
		cm.close();
		@SuppressWarnings("unchecked")
		TypedProcessorList<String, String> tpl = (TypedProcessorList<String, String>) cm.lookup("typedProcessors");
		List<String> values = tpl.process("Omelet");
		assertEquals(2, values.size());

		assertEquals("McOmeletFace", values.get(0));
		assertEquals("telemO-1170105035", values.get(1));
	}
}