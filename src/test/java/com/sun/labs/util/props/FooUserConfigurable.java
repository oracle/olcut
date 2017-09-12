package com.sun.labs.util.props;

/**
 * A User of Foo's.
 *
 * A Configurable with an inner Configurable.
 */
public class FooUserConfigurable implements Configurable {

    @ConfigurableName
    private String name = "user";

    @Config
    private FooConfigurable foo;

    public FooUserConfigurable() { }

    public FooUserConfigurable(FooConfigurable foo) {
        this.foo = foo;
    }

    public FooUserConfigurable(String name, FooConfigurable foo) {
        this.name = name;
        this.foo = foo;
    }

    public FooConfigurable getFoo() {
        return foo;
    }
}
