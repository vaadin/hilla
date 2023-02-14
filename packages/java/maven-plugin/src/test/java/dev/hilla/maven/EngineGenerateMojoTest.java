package dev.hilla.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import dev.hilla.internal.EngineConfiguration;
import dev.hilla.internal.GeneratorConfiguration;
import dev.hilla.internal.GeneratorProcessor;
import dev.hilla.internal.ParserClassPathConfiguration;
import dev.hilla.internal.ParserConfiguration;
import dev.hilla.internal.ParserProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EngineGenerateMojoTest extends AbstractMojoTest {

    @Test
    public void should_RunParserAndGenerator() throws Exception {
        var buildDir = getTemporaryDirectory().resolve("build");
        Files.createDirectories(buildDir);

        Files.copy(Path.of(Objects.requireNonNull(
                getClass().getResource(EngineConfiguration.RESOURCE_NAME)).toURI()),
            buildDir.resolve(EngineConfiguration.RESOURCE_NAME));

        var engineConfiguration = EngineConfiguration.load(buildDir.toFile());
        assertNotNull(engineConfiguration, "expected EngineConfiguration to load correctly");
        engineConfiguration.setBaseDir(getTemporaryDirectory());

        var mojoBuildDirectory = new File(
            Objects.requireNonNull(getClass().getResource("")).toURI());

        try (var mockedConstructionParser = Mockito.mockConstruction(
            ParserProcessor.class,
            Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF),
            (mock, context) -> {
                // Verify ParserProcessor constructor arguments
                assertEquals(3, context.arguments().size(),
                    "expected 3 ParserProcessor arguments");

                var baseDir = (Path) context.arguments().get(0);
                assertEquals(getTemporaryDirectory(), baseDir);

                // Verify class loader argument
                var classLoader = (ClassLoader) context.arguments().get(1);
                assertInstanceOf(URLClassLoader.class, classLoader);
                assertArrayEquals(new URL[] { new File("target/test-classes").toURI().toURL() },
                    ((URLClassLoader) classLoader).getURLs());

                var classPath = (Set<?>) context.arguments().get(2);
                assertEquals(Set.of("target/test-classes"), classPath);
            });
            var mockedConstructionGenerator = Mockito.mockConstruction(
                GeneratorProcessor.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF),
                ((mock, context) -> {
                    // Verify GeneratorProcessor arguments
                    assertEquals(1, context.arguments().size(),
                        "expected 1 GeneratorProcessor argument");

                    var baseDir = (Path) context.arguments().get(0);
                    assertEquals(getTemporaryDirectory(), baseDir);
                }));
            var mockedStaticEngineConfiguration = Mockito.mockStatic(
                EngineConfiguration.class)) {
            mockedStaticEngineConfiguration.when(
                    () -> EngineConfiguration.load(Mockito.eq(mojoBuildDirectory)))
                .thenReturn(engineConfiguration);

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate", getTestConfigurartion());
            engineGenerateMojo.execute();

            assertEquals(1, mockedConstructionParser.constructed().size(), "expected to construct " +
                "ParserProcessor");
            var parserProcessor = mockedConstructionParser.constructed().get(0);

            assertEquals(1, mockedConstructionParser.constructed().size(), "expected to construct " +
                "GeneratorProcessor");
            var generatorProcessor = mockedConstructionGenerator.constructed().get(0);

            var inOrder = Mockito.inOrder(parserProcessor, generatorProcessor);

            // Verify ParserConfiguration is applied to ParserProcessor
            var classPathCaptor = ArgumentCaptor.forClass(
                ParserClassPathConfiguration.class);
            inOrder.verify(parserProcessor)
                .classPath(classPathCaptor.capture());
            assertEquals("maven-test-class-path",
                classPathCaptor.getValue().getValue());
            inOrder.verify(parserProcessor).endpointAnnotation("dev.hilla.maven.Endpoint");
            inOrder.verify(parserProcessor).endpointExposedAnnotation("dev.hilla.maven.EndpointExposed");
            inOrder.verify(parserProcessor).openAPIBase("openapi-maven-test-base.json");
            var parserPluginsCaptor = ArgumentCaptor.forClass(
                ParserConfiguration.Plugins.class);
            inOrder.verify(parserProcessor).plugins(parserPluginsCaptor.capture());
            assertEquals(
                engineConfiguration.getParser().getPlugins().orElseThrow(),
                parserPluginsCaptor.getValue());
            inOrder.verify(parserProcessor).process();

            // Verify GeneratorConfiguration is applied to ParserProcessor
            var generatorPluginsCaptor =
                ArgumentCaptor.forClass(GeneratorConfiguration.Plugins.class);
            inOrder.verify(generatorProcessor).plugins(generatorPluginsCaptor.capture());
            assertEquals(engineConfiguration.getGenerator().getPlugins().orElseThrow(),
                generatorPluginsCaptor.getValue());
            inOrder.verify(generatorProcessor).process();
        }
    }
}
