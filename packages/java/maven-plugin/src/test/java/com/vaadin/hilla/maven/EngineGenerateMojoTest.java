package com.vaadin.hilla.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.GeneratorProcessor;

public class EngineGenerateMojoTest extends AbstractMojoTest {

    @Test
    public void should_RunGenerator() throws Exception {
        try (var mockedConstructionGenerator = Mockito.mockConstruction(
                GeneratorProcessor.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF),
                ((mock, context) -> {
                    // Verify GeneratorProcessor arguments
                    assertEquals(1, context.arguments().size(),
                            "expected 1 GeneratorProcessor argument");

                    // Verify configuration argument
                    var conf = (EngineAutoConfiguration) context.arguments()
                            .get(0);
                    verifyConfiguration(conf);
                }));) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            engineGenerateMojo
                    .setPluginContext(Map.of("project", getMavenProject()));
            engineGenerateMojo.execute();

            assertEquals(1, mockedConstructionGenerator.constructed().size(),
                    "expected to construct GeneratorProcessor");
            var generatorProcessor = mockedConstructionGenerator.constructed()
                    .get(0);

            Mockito.verify(generatorProcessor).process(List.of());
        }
    }

    private void verifyConfiguration(EngineAutoConfiguration conf) {
        assertEquals(conf.getBaseDir(), getTemporaryDirectory());
    }
}
