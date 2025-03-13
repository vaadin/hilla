package com.vaadin.hilla.engine;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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
        var conf = EngineConfiguration.DEFAULT.setClassFinder(classFinder);
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
        var conf = EngineConfiguration.DEFAULT.setClassFinder(classFinder)
                .setEndpointAnnotationNames(
                        BrowserCallableEndpoint.class.getName());
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.findEndpointClasses(conf))
                    .thenThrow(ParserException.class);
            assertEquals(List.of(EndpointFromClassFinder.class),
                    conf.getBrowserCallableFinder().findBrowserCallables());
        }
    }

    // /**
    // * Test that the service class overrides all public methods of the
    // * EngineConfiguration class. This is a sort of compile-like test to
    // ensure
    // * that the service class is kept up-to-date with the superclass.
    // */
    // @Test
    // public void serviceShouldOverrideAllPublicMethods() {
    // var engineConfigurationClass = EngineConfiguration.class;
    // var engineConfigurationServiceClass = EngineConfiguration.Service.class;

    // var nonOverriddenMethods = Arrays
    // .stream(engineConfigurationClass.getDeclaredMethods())
    // .filter(method -> Modifier.isPublic(method.getModifiers()))
    // .filter(method -> !Modifier.isStatic(method.getModifiers()))
    // .filter(method -> !Modifier.isFinal(method.getModifiers()))
    // .filter(method -> !method.getName().equals("getDefault")
    // && !method.getName().equals("setDefault"))
    // .filter(method -> {
    // try {
    // return engineConfigurationServiceClass
    // .getDeclaredMethod(method.getName(),
    // method.getParameterTypes()) == null;
    // } catch (NoSuchMethodException e) {
    // return true;
    // }
    // }).toList();

    // assertTrue(nonOverriddenMethods.isEmpty(),
    // "Service class should override all public methods of the
    // EngineConfiguration class. "
    // + "The following methods are not overridden: "
    // + nonOverriddenMethods);
    // }

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
