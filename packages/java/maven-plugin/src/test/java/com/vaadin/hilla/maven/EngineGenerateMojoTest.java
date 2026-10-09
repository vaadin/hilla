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
package com.vaadin.hilla.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;
import com.vaadin.hilla.engine.TypeScriptProcessor;
import com.vaadin.hilla.generator.model.Generation;

public class EngineGenerateMojoTest extends AbstractMojoTest {
    /**
     * What a run of the parser is made to return, which is what the mojo has to
     * hand the writers.
     */
    private static final Generation GENERATION = new Generation(List.of(),
            List.of(), List.of());

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
                    var conf = (EngineAutoConfiguration) context.arguments()
                            .getFirst();
                    assertEquals(conf.getBaseDir(), getTemporaryDirectory());

                    // What the parser found, so that the test says which
                    // generation the writers are handed rather than none
                    Mockito.doReturn(GENERATION).when(mock).getGeneration();
                });
                var mockedConstructionGenerator = Mockito.mockConstruction(
                        TypeScriptProcessor.class, Mockito.withSettings()
                                .defaultAnswer(Answers.RETURNS_SELF),
                        ((mock, context) -> {
                            // Verify TypeScriptProcessor arguments
                            assertEquals(1, context.arguments().size(),
                                    "expected 1 TypeScriptProcessor argument");

                            // Verify configuration argument
                            var conf = (EngineAutoConfiguration) context
                                    .arguments().getFirst();
                            assertEquals(conf.getBaseDir(),
                                    getTemporaryDirectory());
                        }));) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            engineGenerateMojo
                    .setPluginContext(Map.of("project", getMavenProject()));
            engineGenerateMojo.execute();

            assertEquals(1, mockedConstructionParser.constructed().size(),
                    "expected to construct " + "ParserProcessor");
            var parserProcessor = mockedConstructionParser.constructed()
                    .getFirst();

            assertEquals(1, mockedConstructionGenerator.constructed().size(),
                    "expected to construct " + "TypeScriptProcessor");
            var typeScriptProcessor = mockedConstructionGenerator.constructed()
                    .getFirst();

            var inOrder = Mockito.inOrder(parserProcessor, typeScriptProcessor);
            inOrder.verify(parserProcessor).process(List.of());
            // The writers are handed what the parser found, which is what the
            // TypeScript of the endpoints is written from
            inOrder.verify(typeScriptProcessor).process(GENERATION);
        }
    }

    @Test
    public void should_setConfiguration() throws Exception {
        var mainClass = "com.vaadin.hilla.test.MainClass";
        var sourceClasses = new String[] {
                "com.vaadin.hilla.test.TestConfiguration",
                "com.vaadin.hilla.test.TestLibraryConfiguration", };

        try (var mockedConstructionParser = Mockito.mockConstruction(
                ParserProcessor.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, context) -> {
                    // Verify ParserProcessor constructor arguments
                    assertEquals(1, context.arguments().size(),
                            "expected 1 ParserProcessor argument");

                    // Verify configuration argument
                    var conf = (EngineAutoConfiguration) context.arguments()
                            .getFirst();
                    assertEquals(mainClass, conf.getMainClass());
                    assertEquals(List.of(sourceClasses),
                            conf.getSourceClasses());
                });
                var mockedConstructionGenerator = Mockito.mockConstruction(
                        TypeScriptProcessor.class, Mockito.withSettings()
                                .defaultAnswer(Answers.RETURNS_SELF),
                        ((mock, context) -> {
                            // Verify TypeScriptProcessor arguments
                            assertEquals(1, context.arguments().size(),
                                    "expected 1 TypeScriptProcessor argument");

                            // Verify configuration argument
                            var conf = (EngineAutoConfiguration) context
                                    .arguments().getFirst();
                            assertEquals(mainClass, conf.getMainClass());
                            assertEquals(List.of(sourceClasses),
                                    conf.getSourceClasses());
                        }));) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            setVariableValueToObject(engineGenerateMojo, "mainClass",
                    mainClass);
            setVariableValueToObject(engineGenerateMojo, "sourceClasses",
                    sourceClasses);
            engineGenerateMojo
                    .setPluginContext(Map.of("project", getMavenProject()));
            engineGenerateMojo.execute();
        }
    }
}
