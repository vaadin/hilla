package dev.hilla.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class EngineGenerateMojoTest extends AbstractMojoTest {

    @Test
    public void should_RunParserAndGenerator() throws Exception {
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
                .thenReturn(getEngineConfiguration());

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
            inOrder.verify(parserProcessor).apply(getEngineConfiguration().getParser());
            inOrder.verify(parserProcessor).process();

            // Verify GeneratorConfiguration is applied to ParserProcessor
            inOrder.verify(generatorProcessor).apply(getEngineConfiguration().getGenerator());
            inOrder.verify(generatorProcessor).process();
        }
    }
}
