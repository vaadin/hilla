package com.vaadin.hilla;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/**
 * Validates that all endpoint classes in sources are annotated with
 * {@link InternalBrowserCallable} to not trigger endpoint generator if
 * endpoints are not actually used.
 */
public class InternalBrowserCallableTest {

    private static final String BASE_PACKAGE = "com.vaadin";

    @Test
    public void testBrowserCallableAndEndpointAnnotationsMarkedAsInternal() {
        Reflections reflections = new Reflections(BASE_PACKAGE,
                Scanners.TypesAnnotated);
        Set<Class<?>> annotatedClasses = reflections
                .getTypesAnnotatedWith(BrowserCallable.class);
        Set<Class<?>> endpointClasses = reflections
                .getTypesAnnotatedWith(Endpoint.class);
        annotatedClasses.addAll(endpointClasses);

        for (Class<?> clazz : annotatedClasses) {
            if (!clazz.getName().contains(".test.") && !clazz
                    .isAnnotationPresent(InternalBrowserCallable.class)) {
                Assertions.fail("Class " + clazz.getName()
                        + " is annotated with @BrowserCallable or @Endpoint, but missing @InternalBrowserCallable.");
            }
        }
    }

    @Test
    public void testInternalBrowserCallableHasBrowserCallableOrEndpoint()
            throws Exception {
        Reflections reflections = new Reflections(BASE_PACKAGE,
                Scanners.TypesAnnotated);
        Set<Class<?>> annotatedClasses = reflections
                .getTypesAnnotatedWith(InternalBrowserCallable.class);

        for (Class<?> clazz : annotatedClasses) {
            if (!clazz.isAnnotationPresent(BrowserCallable.class)
                    && !clazz.isAnnotationPresent(Endpoint.class)) {
                Assertions.fail("Class " + clazz.getName()
                        + " is annotated with @InternalBrowserCallable and must be annotated with @BrowserCallable or @Endpoint.");
            }
        }
    }
}
