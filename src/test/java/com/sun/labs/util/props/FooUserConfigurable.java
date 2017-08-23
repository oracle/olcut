package com.sun.labs.util.props;

/**
 * A User of Foo's.
 *
 * A Configurable with an inner Configurable.
 */
public class FooUserConfigurable implements Configurable {

    @Config
    private FooConfigurable foo;

    public FooUserConfigurable() { }

    public FooUserConfigurable(FooConfigurable foo) {
        this.foo = foo;
    }

    public FooConfigurable getFoo() {
        return foo;
    }
}
