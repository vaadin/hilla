package com.vaadin.hilla.engine;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

public class EngineConfigurationTest {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BrowserCallableEndpoint {
    }

    private static class EndpointFromAot {
    }

    private static class EndpointFromClassFinder {
    }

    @Test
    public void shouldUseAot() throws Exception {
        var classFinder = mock(ClassFinder.class);
        when(classFinder
                .getAnnotatedClasses((Class<? extends Annotation>) any()))
                .thenThrow(RuntimeException.class);
        var conf = new EngineConfiguration.Builder().classFinder(classFinder)
                .build();
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.findEndpointClasses(conf))
                    .thenReturn(List.of(EndpointFromAot.class));
            assertEquals(List.of(EndpointFromAot.class),
                    conf.getBrowserCallableFinder().findBrowserCallables());
        }
    }

    @Test
    public void shouldFallbackToClassFinder() throws Exception {
        var classFinder = mock(ClassFinder.class);
        when(classFinder
                .getAnnotatedClasses((Class<? extends Annotation>) any()))
                .thenReturn(Set.of(EndpointFromClassFinder.class));
        var conf = new EngineConfiguration.Builder().classFinder(classFinder)
                .endpointAnnotations(BrowserCallableEndpoint.class).build();
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.findEndpointClasses(conf))
                    .thenThrow(ParserException.class);
            assertEquals(List.of(EndpointFromClassFinder.class),
                    conf.getBrowserCallableFinder().findBrowserCallables());
        }
    }
}
