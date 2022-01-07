package dev.hilla.parser.core.dependency;

public class DependencyEntityTwo {
    public DependencyEntityOne foo;

    public DependencyEntityOne circular() {
        return foo;
    }

    public static class InnerClass {
    }
}
