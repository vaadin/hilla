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
                    var conf = (EngineAutoConfiguration) context.arguments()
                            .getFirst();
                    assertEquals(conf.getBaseDir(), getTemporaryDirectory());
                });
                var mockedConstructionGenerator = Mockito.mockConstruction(
                        GeneratorProcessor.class, Mockito.withSettings()
                                .defaultAnswer(Answers.RETURNS_SELF),
                        ((mock, context) -> {
                            // Verify GeneratorProcessor arguments
                            assertEquals(1, context.arguments().size(),
                                    "expected 1 GeneratorProcessor argument");

                            // Verify configuration argument
                            var conf = (EngineAutoConfiguration) context
                                    .arguments().getFirst();
                            assertEquals(conf.getBaseDir(), getTemporaryDirectory());
                        }));) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            engineGenerateMojo
                    .setPluginContext(Map.of("project", getMavenProject()));
            engineGenerateMojo.execute();

            assertEquals(1, mockedConstructionParser.constructed().size(),
                    "expected to construct " + "ParserProcessor");
            var parserProcessor = mockedConstructionParser.constructed().getFirst();

            assertEquals(1, mockedConstructionGenerator.constructed().size(),
                    "expected to construct " + "GeneratorProcessor");
            var generatorProcessor = mockedConstructionGenerator.constructed()
                    .getFirst();

            var inOrder = Mockito.inOrder(parserProcessor, generatorProcessor);
            inOrder.verify(parserProcessor).process(List.of());
            inOrder.verify(generatorProcessor).process();
        }
    }

    @Test
    public void should_setConfiguration() throws Exception {
        var mainClass = "com.vaadin.hilla.test.MainClass";
        var sourceClasses = new String[] {
                "com.vaadin.hilla.test.TestConfiguration",
                "com.vaadin.hilla.test.TestLibraryConfiguration",
        };

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
                    assertEquals(List.of(sourceClasses), conf.getSourceClasses());
                });
             var mockedConstructionGenerator = Mockito.mockConstruction(
                     GeneratorProcessor.class, Mockito.withSettings()
                             .defaultAnswer(Answers.RETURNS_SELF),
                     ((mock, context) -> {
                         // Verify GeneratorProcessor arguments
                         assertEquals(1, context.arguments().size(),
                                 "expected 1 GeneratorProcessor argument");

                         // Verify configuration argument
                         var conf = (EngineAutoConfiguration) context
                                 .arguments().getFirst();
                         assertEquals(mainClass, conf.getMainClass());
                         assertEquals(List.of(sourceClasses), conf.getSourceClasses());
                     }));) {

            // Lookup and initialize mojo
            var engineGenerateMojo = (EngineGenerateMojo) lookupMojo("generate",
                    getTestConfiguration());
            setVariableValueToObject(engineGenerateMojo, "mainClass", mainClass);
            setVariableValueToObject(engineGenerateMojo, "sourceClasses", sourceClasses);
            engineGenerateMojo
                    .setPluginContext(Map.of("project", getMavenProject()));
            engineGenerateMojo.execute();
        }
    }
}
