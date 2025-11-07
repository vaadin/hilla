/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

public class EngineAutoConfigurationTest {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BrowserCallableEndpoint {
        String value() default "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BrowserCallableExposed {
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
        var conf = new EngineAutoConfiguration.Builder()
                .classFinder(classFinder)
                .endpointAnnotations(BrowserCallableEndpoint.class).build();
        assertEquals(List.of(EndpointFromClassFinder.class),
                conf.getBrowserCallableFinder().find(conf));
    }

    @Test
    public void shouldUseAotWhenNoClassFinder() throws Exception {
        // classFinder is null by default in configuration
        var conf = EngineAutoConfiguration.getDefault();
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.find(conf))
                    .thenReturn(List.of(EndpointFromAot.class));
            assertEquals(List.of(EndpointFromAot.class),
                    conf.getBrowserCallableFinder().find(conf));
        }
    }

    @Test
    public void shouldUseAotWhenClassFinderThrowsException() throws Exception {
        var classFinder = mock(ClassFinder.class);
        when(classFinder
                .getAnnotatedClasses((Class<? extends Annotation>) any()))
                .thenReturn(Set.of(EndpointFromClassFinder.class,
                        EndpointFromClassFinderWithCustomName.class));
        var conf = new EngineAutoConfiguration.Builder()
                .classFinder(classFinder)
                .endpointAnnotations(BrowserCallableEndpoint.class).build();
        try (var aotMock = mockStatic(AotBrowserCallableFinder.class)) {
            when(AotBrowserCallableFinder.find(conf))
                    .thenReturn(List.of(EndpointFromAot.class));
            assertEquals(List.of(EndpointFromAot.class),
                    conf.getBrowserCallableFinder().find(conf));
        }
    }

    private static final ClassLoader testClassLoader1 = mock(ClassLoader.class);
    private static final ClassLoader testClassLoader2 = mock(ClassLoader.class);

    public static class FirstLoadedConfiguration
            implements EngineConfiguration {
        @Override
        public ClassLoader getClassLoader(ClassLoader defaultClassLoader) {
            // avoid interfering with other tests
            return defaultClassLoader == testClassLoader1 ? testClassLoader2
                    : defaultClassLoader;
        }
    }

    public static class SecondLoadedConfiguration
            implements EngineConfiguration {
        @Override
        public ClassLoader getClassLoader(ClassLoader defaultClassLoader) {
            return defaultClassLoader;
        }
    }

    @Test
    public void shouldThrowWhenMultipleCustomConfigurations() {
        var ex = assertThrows(ConfigurationException.class,
                () -> EngineAutoConfiguration.pick(
                        new FirstLoadedConfiguration(),
                        new SecondLoadedConfiguration()));
        assertTrue(ex.getMessage().contains(
                "Multiple EngineConfiguration implementations found:"));
        assertTrue(ex.getMessage().contains("FirstLoadedConfiguration"));
        assertTrue(ex.getMessage().contains("SecondLoadedConfiguration"));
    }

    @Test
    public void shouldUseServiceLoader() {
        // expected to use TestService loaded from META-INF/services
        var conf = new EngineAutoConfiguration.Builder()
                .classLoader(testClassLoader1).build();
        assertEquals(testClassLoader2, conf.getClassLoader());
    }

    @Test
    public void builderShouldRetainAllPassedValues() {
        var baseDir = Path.of("/tmp/base");
        var buildDir = Path.of("/tmp/build");
        var classesDirs = List.of(Path.of("/tmp/classes1"),
                Path.of("/tmp/classes2"));
        var classpathStrings = Set.of("/tmp/cp1", "/tmp/cp2");
        var generator = new GeneratorConfiguration();
        var parser = new ParserConfiguration();
        var outputDir = Path.of("/tmp/output");
        var groupId = "test.group";
        var artifactId = "test-artifact";
        var mainClass = "com.example.Main";
        var productionMode = true;
        var nodeCommand = "node-custom";
        var classFinder = mock(ClassFinder.class);
        var classLoader = mock(ClassLoader.class);
        // when(classFinder.getClassLoader()).thenReturn(classLoader);
        var browserCallableFinder = (BrowserCallableFinder) (conf) -> List
                .of(EndpointFromAot.class);
        var endpointAnnotation = BrowserCallableEndpoint.class;
        var endpointExposedAnnotation = BrowserCallableExposed.class;

        var config = new EngineAutoConfiguration.Builder().baseDir(baseDir)
                .buildDir(buildDir).classesDirs(classesDirs)
                .classpath(classpathStrings).generator(generator).parser(parser)
                .outputDir(outputDir).groupId(groupId).artifactId(artifactId)
                .mainClass(mainClass).productionMode(productionMode)
                .nodeCommand(nodeCommand).classFinder(classFinder)
                .classLoader(classLoader)
                .browserCallableFinder(browserCallableFinder)
                .endpointAnnotations(endpointAnnotation)
                .endpointExposedAnnotations(endpointExposedAnnotation).build();

        assertEquals(baseDir, config.getBaseDir());
        assertEquals(buildDir, config.getBuildDir());
        assertEquals(classesDirs, config.getClassesDirs());
        assertEquals(
                classpathStrings.stream().map(Path::of)
                        .collect(java.util.stream.Collectors.toSet()),
                config.getClasspath());
        assertSame(generator, config.getGenerator());
        assertSame(parser, config.getParser());
        assertEquals(outputDir, config.getOutputDir());
        assertEquals(groupId, config.getGroupId());
        assertEquals(artifactId, config.getArtifactId());
        assertEquals(mainClass, config.getMainClass());
        assertTrue(config.isProductionMode());
        assertEquals(nodeCommand, config.getNodeCommand());
        assertSame(classFinder, config.getClassFinder());
        assertSame(classLoader, config.getClassLoader());
        assertSame(browserCallableFinder, config.getBrowserCallableFinder());
        assertEquals(List.of(endpointAnnotation),
                config.getEndpointAnnotations());
        assertEquals(List.of(endpointExposedAnnotation),
                config.getEndpointExposedAnnotations());
    }
}
