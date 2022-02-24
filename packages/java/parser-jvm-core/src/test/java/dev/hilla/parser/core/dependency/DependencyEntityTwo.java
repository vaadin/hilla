package dev.hilla.parser.core.dependency;

import javax.annotation.Nonnull;

public class DependencyEntityTwo {
    public DependencyEntityOne foo;

    @Nonnull
    public DependencyEntityOne circular() {
        return foo;
    }

    public static class InnerClass {
    }
}
