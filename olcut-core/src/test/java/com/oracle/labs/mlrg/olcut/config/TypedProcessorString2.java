package com.oracle.labs.mlrg.olcut.config;

import java.util.Random;

public class TypedProcessorString2 implements TypedProcessor<String, String>{

	@Config
	private int randomSeed;
	
	private Random random;
	
	public TypedProcessorString2() {}
	
	@Override
	public void postConfig() {
		random = new Random(randomSeed);
	}
	
	@Override
	public String process(String p) {
		return new StringBuilder(p).reverse().toString()+random.nextInt();
	}

	
}
