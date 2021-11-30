package com.vaadin.fusion.parser.core.dependency;

public class DependencyEntityOne {
    public String foo;
    private int bar;

    public DependencyEntityTwo circular() {
        return new DependencyEntityTwo();
    }

    public String getFoo() {
        return foo;
    }

    public void setBar(int value) {
        bar = value;
    }
}
