package com.oracle.labs.mlrg.olcut.config;

import java.util.function.Supplier;

public class LazyGlobalProperty extends GlobalProperty {

    private final Supplier<String> supplier;

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
    public String toString() {
        return getValue();
    }

}
