package com.vaadin.hilla.parser.core.dependency;

import org.jspecify.annotations.NonNull;

public class DependencyEntityTwo {
    public DependencyEntityOne foo2;

    @NonNull
    public DependencyEntityOne circular() {
        return foo2;
    }

    public static class InnerClass {
        public String innerClassMember;
    }
}
