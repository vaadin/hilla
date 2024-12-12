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
                    assertEquals(1, context.arguments().size(),
                            "expected 1 ParserProcessor argument");

                    // Verify configuration argument
                    var conf = (EngineConfiguration) context.arguments().get(0);
                    verifyConfiguration(conf);
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
                            verifyConfiguration(conf);
                        }));) {

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

    private void verifyConfiguration(EngineConfiguration conf) {
        assertEquals(conf.getBaseDir(), getTemporaryDirectory());
    }
}
