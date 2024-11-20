package com.vaadin.hilla.maven;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserProcessor;

public class EngineGenerateMojoTest extends AbstractMojoTest {

    @Test
    public void should_RunParserAndGenerator() throws Exception {
        try (var mockedConstructionParser = Mockito.mockConstruction(
                ParserProcessor.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, context) -> {
                    // Verify ParserProcessor constructor arguments
                    assertEquals(2, context.arguments().size(),
                            "expected 2 ParserProcessor arguments");

                    // Verify configuration argument
                    var conf = (EngineConfiguration) context.arguments().get(0);
                    assertEquals(conf, getEngineConfiguration());

                    // Verify class loader argument
                    var classLoader = (ClassLoader) context.arguments().get(1);
                    assertInstanceOf(URLClassLoader.class, classLoader);
                    assertEquals(classLoader.getParent(),
                            EngineGenerateMojo.class.getClassLoader());
                    assertArrayEquals(
                            new URL[] { getTemporaryDirectory()
                                    .resolve("build/classes").toUri().toURL(),
                                    getTemporaryDirectory()
                                            .resolve("build/test-classes")
                                            .toUri().toURL() },
                            ((URLClassLoader) classLoader).getURLs());
                });
                var mockedConstructionGenerator = Mockito.mockConstruction(
                        GeneratorProcessor.class, Mockito.withSettings()
                                .defaultAnswer(Answers.RETURNS_SELF),
                        ((mock, context) -> {
                            // Verify GeneratorProcessor arguments
                            assertEquals(3, context.arguments().size(),
                                    "expected 3 GeneratorProcessor arguments");

                            // Verify configuration argument
                            var conf = (EngineConfiguration) context.arguments()
                                    .get(0);
                            assertEquals(conf, getEngineConfiguration());
                        }));

                var mockedStaticEngineConfiguration = Mockito
                        .mockStatic(EngineConfiguration.class)) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            setVariableValueToObject(engineGenerateMojo, "project",
                    getMavenProject());
            engineGenerateMojo.execute();

            assertEquals(1, mockedConstructionParser.constructed().size(),
                    "expected to construct " + "ParserProcessor");
            var parserProcessor = mockedConstructionParser.constructed().get(0);

            assertEquals(1, mockedConstructionGenerator.constructed().size(),
                    "expected to construct " + "GeneratorProcessor");
            var generatorProcessor = mockedConstructionGenerator.constructed()
                    .get(0);

            var inOrder = Mockito.inOrder(parserProcessor, generatorProcessor);
            inOrder.verify(parserProcessor).process(List.of());
            inOrder.verify(generatorProcessor).process();
        }
    }
}
