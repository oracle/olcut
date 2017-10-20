package com.oracle.labs.mlrg.olcut.config;

import java.util.function.Supplier;

public class LazyGlobalProperty extends GlobalProperty {

    private Supplier<String> supplier;

    public LazyGlobalProperty(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    @Override
    public synchronized String getValue() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    @Override
    public synchronized void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
