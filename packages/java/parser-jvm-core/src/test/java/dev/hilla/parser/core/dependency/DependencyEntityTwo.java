package dev.hilla.parser.core.dependency;

import javax.annotation.Nonnull;

public class DependencyEntityTwo {
    public DependencyEntityOne foo2;

    @Nonnull
    public DependencyEntityOne circular() {
        return foo2;
    }

    public static class InnerClass {
        public String innerClassMember;
    }
}
