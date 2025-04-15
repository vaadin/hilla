package com.vaadin.hilla.engine;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

public class EngineConfigurationTest {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BrowserCallableEndpoint {
        String value() default "";
    }

    @BrowserCallableEndpoint
    private static class EndpointFromAot {
    }

    @BrowserCallableEndpoint
    private static class EndpointFromClassFinder {
    }

    @BrowserCallableEndpoint("EndpointFromClassFinder")
    private static class EndpointFromClassFinderWithCustomName {
    }

    @Test
    public void shouldUseLookupByDefault() throws Exception {
        var classFinder = mock(ClassFinder.class);
        when(classFinder
                .getAnnotatedClasses((Class<? extends Annotation>) any()))
                .thenReturn(Set.of(EndpointFromClassFinder.class));
        var conf = EngineConfiguration.STATE.setClassFinder(classFinder)
                .setEndpointAnnotationNames(
                        BrowserCallableEndpoint.class.getName());
        assertEquals(List.of(EndpointFromClassFinder.class),
                conf.findBrowserCallables());
    }

    @Test
    public void shouldUseAotWhenNoClassFinder() throws Exception {
        // classFinder is null by default in configuration
        var conf = EngineConfiguration.STATE;
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.find(conf))
                    .thenReturn(List.of(EndpointFromAot.class));
            assertEquals(List.of(EndpointFromAot.class),
                    conf.findBrowserCallables());
        }
    }

    @Test
    public void shouldUseAotWhenClassFinderThrowsException() throws Exception {
        var classFinder = mock(ClassFinder.class);
        when(classFinder
                .getAnnotatedClasses((Class<? extends Annotation>) any()))
                .thenReturn(Set.of(EndpointFromClassFinder.class,
                        EndpointFromClassFinderWithCustomName.class));
        var conf = EngineConfiguration.STATE.setClassFinder(classFinder)
                .setEndpointAnnotationNames(
                        BrowserCallableEndpoint.class.getName());
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.find(conf))
                    .thenReturn(List.of(EndpointFromAot.class));
            assertEquals(List.of(EndpointFromAot.class),
                    conf.findBrowserCallables());
        }
    }

    private static class TestService implements EngineConfiguration {
        @Override
        public String getGroupId() {
            return "com.example";
        }
    }

    @Test
    public void shouldLoadCustomConfiguration() {
        try (var staticServiceLoaderMock = mockStatic(ServiceLoader.class)) {
            var serviceLoaderMock = mock(ServiceLoader.class);
            when(ServiceLoader.load(EngineConfiguration.class))
                    .thenReturn(serviceLoaderMock);
            var providerMock = mock(ServiceLoader.Provider.class);
            when(serviceLoaderMock.stream())
                    .thenReturn(Stream.of(providerMock));
            when(providerMock.get()).thenReturn(new TestService());
            var conf = EngineConfiguration.load();
            assertEquals("com.example", conf.getGroupId());
        }
    }

    private static class OtherTestService implements EngineConfiguration {
        @Override
        public String getGroupId() {
            return "com.other";
        }
    }

    @Test
    public void shouldThrowWhenMultipleCustomConfigurations() {
        try (var staticServiceLoaderMock = mockStatic(ServiceLoader.class)) {
            var serviceLoaderMock = mock(ServiceLoader.class);
            when(ServiceLoader.load(EngineConfiguration.class))
                    .thenReturn(serviceLoaderMock);
            var providerMock = mock(ServiceLoader.Provider.class);
            var otherProviderMock = mock(ServiceLoader.Provider.class);
            when(serviceLoaderMock.stream())
                    .thenReturn(Stream.of(providerMock, otherProviderMock));
            when(providerMock.get()).thenReturn(new TestService());
            when(otherProviderMock.get()).thenReturn(new OtherTestService());
            assertThrows(ConfigurationException.class,
                    EngineConfiguration::load);
        }
    }
}
