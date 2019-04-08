package com.oracle.labs.mlrg.olcut.config;

public class TypedProcessorString implements TypedProcessor<String, String>{

	@Config
	private String prefix;
	
	@Config
	private String suffix;
	
	@Override
	public String process(String p) {
		return prefix+p+suffix;
	}

	
}
