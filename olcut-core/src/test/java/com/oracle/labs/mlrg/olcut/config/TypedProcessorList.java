package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.List;

public class TypedProcessorList<PARAM, RETURN> implements Configurable {

	@Config
	private List<TypedProcessor<PARAM, RETURN>> processors;

	public List<TypedProcessor<PARAM, RETURN>> getProcessors() {
		return processors;
	}
	
	public List<RETURN> process(PARAM p){
		List<RETURN> returnValues = new ArrayList<>();
		for (TypedProcessor<PARAM, RETURN> typedProcessor : processors) {
			returnValues.add(typedProcessor.process(p));
		}
		return returnValues;
	}
	
}