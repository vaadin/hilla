package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;

public class LookupBrowserCallableFinderTest {
    @Endpoint
    public static class AnnotatedWithEndpoint {
    }

    @BrowserCallable
    public static class AnnotatedWithBrowserCallable {
    }

    @Endpoint("CustomNameClash")
    public static class AnnotatedWithEndpointAndCustomName {
    }

    @BrowserCallable
    public static class CustomNameClash {
    }

    @Endpoint
    public static class NameClash {
    }

    private EngineConfiguration engineConfigurationMock;
    private ClassFinder classFinderMock;

    @BeforeEach
    public void setUp() {
        engineConfigurationMock = mock(EngineConfiguration.class);
        when(engineConfigurationMock.getEndpointAnnotations())
                .thenReturn(List.of(Endpoint.class, BrowserCallable.class));
        classFinderMock = mock(ClassFinder.class);
        when(engineConfigurationMock.getClassFinder())
                .thenReturn(classFinderMock);
    }

    @Test
    public void shouldReturnAllBrowserCallables()
            throws ExecutionFailedException {
        when(classFinderMock.getAnnotatedClasses(Endpoint.class))
                .thenReturn(Set.of(AnnotatedWithEndpoint.class,
                        AnnotatedWithEndpointAndCustomName.class));
        when(classFinderMock.getAnnotatedClasses(BrowserCallable.class))
                .thenReturn(Set.of(AnnotatedWithBrowserCallable.class));
        var browserCallables = LookupBrowserCallableFinder
                .find(engineConfigurationMock);
        var expectedBrowserCallables = Set.of(
                AnnotatedWithBrowserCallable.class, AnnotatedWithEndpoint.class,
                AnnotatedWithEndpointAndCustomName.class);
        assertTrue(browserCallables.containsAll(expectedBrowserCallables)
                && expectedBrowserCallables.containsAll(browserCallables));
    }

    @Test
    public void shouldThrowWhenTwoBrowserCallablesHaveSameName() {
        when(classFinderMock.getAnnotatedClasses(Endpoint.class))
                .thenReturn(Set.of(NameClash.class));
        when(classFinderMock.getAnnotatedClasses(BrowserCallable.class))
                .thenReturn(Set.of(com.vaadin.hilla.engine.NameClash.class));
        var exception = assertThrows(
            IllegalStateException.class,
            () -> LookupBrowserCallableFinder.find(engineConfigurationMock)
        );
        assertTrue(exception.getMessage().contains("NameClash"));
    }

    @Test
    public void shouldThrowWhenTwoBrowserCallablesHaveSameNameWithCustomName() {
        when(classFinderMock.getAnnotatedClasses(Endpoint.class))
                .thenReturn(Set.of(NameClash.class,
                        AnnotatedWithEndpointAndCustomName.class));
        when(classFinderMock.getAnnotatedClasses(BrowserCallable.class))
                .thenReturn(Set.of(CustomNameClash.class));
        var exception = assertThrows(
            IllegalStateException.class,
            () -> LookupBrowserCallableFinder.find(engineConfigurationMock)
        );
        assertTrue(exception.getMessage().contains("CustomNameClash"));
    }
}
