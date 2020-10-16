package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Test class for provenance null handling.
 */
public final class TestProvenancableConfigurable implements Configurable, Provenancable<ConfiguredObjectProvenance> {

    // This field is always null to test the extraction of null fields.
    @Config
    public ExampleProvenancableConfigurable nullField = null;

    @Config
    public int someIntValue = 5;

    @Config
    public List<Integer> someListOfInts = new ArrayList<>();

    public TestProvenancableConfigurable() {}

    public TestProvenancableConfigurable(int intValue, List<Integer> listOfInts) {
        someIntValue = intValue;
        someListOfInts = new ArrayList<>(listOfInts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestProvenancableConfigurable that = (TestProvenancableConfigurable) o;
        return someIntValue == that.someIntValue &&
                Objects.equals(nullField, that.nullField) &&
                someListOfInts.equals(that.someListOfInts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nullField, someIntValue, someListOfInts);
    }

    @Override
    public ConfiguredObjectProvenance getProvenance() {
        return new ConfiguredObjectProvenanceImpl(this,"test-configurable-provenancable");
    }
}
