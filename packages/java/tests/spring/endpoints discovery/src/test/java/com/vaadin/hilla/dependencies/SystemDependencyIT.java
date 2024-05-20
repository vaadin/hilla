package com.vaadin.hilla.dependencies;

import org.junit.Test;

public class SystemDependencyIT {
    @Test
    public void shouldFindClassFromSystemDependency() {
        var entityClass = "com.example.library.unpublished.SomeEntity";

        try {
            Class.forName(entityClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Could not find class " + entityClass
                    + " from system dependency", e);
        }
    }
}
