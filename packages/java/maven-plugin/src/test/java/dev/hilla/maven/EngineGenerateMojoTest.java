package dev.hilla.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

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
                            new URL[] { new File("build/test-classes").toURI()
                                    .toURL() },
                            ((URLClassLoader) classLoader).getURLs());
                });
                var mockedConstructionGenerator = Mockito.mockConstruction(
                        GeneratorProcessor.class, Mockito.withSettings()
                                .defaultAnswer(Answers.RETURNS_SELF),
                        ((mock, context) -> {
                            // Verify GeneratorProcessor arguments
                            assertEquals(1, context.arguments().size(),
                                    "expected 1 GeneratorProcessor argument");

                            // Verify configuration argument
                            var conf = (EngineConfiguration) context.arguments()
                                    .get(0);
                            assertEquals(conf, getEngineConfiguration());
                        }));

                var mockedStaticEngineConfiguration = Mockito
                        .mockStatic(EngineConfiguration.class)) {

            // Use reference EngineConfiguration
            mockedStaticEngineConfiguration
                    .when(() -> EngineConfiguration
                            .load(Mockito.eq(getBuildDirectory().toFile())))
                    .thenReturn(getEngineConfiguration());

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfigurartion());
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
            inOrder.verify(parserProcessor).process();
            inOrder.verify(generatorProcessor).process();
        }
    }
}
