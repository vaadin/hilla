package com.vaadin.hilla.parser.core.dependency;

public class DependencyEntityOne {
    public String foo;
    private int bar;

    private DependencyEntityThree dependencyEntityThree = new DependencyEntityThree();

    public DependencyEntityTwo circular() {
        return new DependencyEntityTwo();
    }

    public DependencyEntityThree getDependencyEntityThree() {
        return dependencyEntityThree;
    }

    public String getFoo() {
        return foo;
    }

    public void setBar(int value) {
        bar = value;
    }
}
