package com.oracle.labs.mlrg.olcut.config;

public interface TypedProcessor<PARAM, RETURN> extends Configurable {
	public RETURN process(PARAM p);
}