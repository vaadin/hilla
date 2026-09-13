/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * Verifies what a change has to touch to be worth generating the TypeScript of
 * the endpoints again, which is what the browser callable classes and the types
 * they expose are.
 */
public class EndpointCodeGeneratorTest {
    @BrowserCallable
    public static class ChangedEndpoint {
        public Changed get() {
            return null;
        }
    }

    public static class Changed {
        private String name;

        public String getName() {
            return name;
        }
    }

    @TempDir
    private File projectFolder;

    @Test
    public void should_KnowTheClassesTheEndpointsAreWrittenFrom() {
        var configuration = Mockito.mock(ApplicationConfiguration.class);
        Mockito.doReturn(projectFolder).when(configuration).getProjectFolder();
        Mockito.doReturn("build").when(configuration).getBuildFolder();
        Mockito.doReturn(new File(projectFolder, "frontend"))
                .when(configuration).getFrontendFolder();

        var applicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.doReturn(Map.of("endpoint", new ChangedEndpoint()))
                .when(applicationContext)
                .getBeansWithAnnotation(BrowserCallable.class);
        Mockito.doReturn(Map.of()).when(applicationContext)
                .getBeansWithAnnotation(Endpoint.class);

        try (var applicationConfiguration = Mockito
                .mockStatic(ApplicationConfiguration.class);
                var contextProvider = Mockito
                        .mockStatic(ApplicationContextProvider.class)) {
            applicationConfiguration
                    .when(() -> ApplicationConfiguration
                            .get(Mockito.any(VaadinContext.class)))
                    .thenReturn(configuration);
            contextProvider
                    .when(() -> ApplicationContextProvider
                            .runOnContext(Mockito.any()))
                    .thenAnswer(invocation -> {
                        invocation.<Consumer<ApplicationContext>> getArgument(0)
                                .accept(applicationContext);
                        return null;
                    });

            var classes = new EndpointCodeGenerator(
                    Mockito.mock(VaadinContext.class),
                    Mockito.mock(EndpointController.class))
                    .getClassesUsedInEndpoints();

            assertTrue(classes.isPresent(),
                    "The browser callable classes have been walked");
            assertEquals(
                    Set.of(ChangedEndpoint.class.getName(),
                            Changed.class.getName()),
                    classes.get(),
                    "The endpoint and what it sends are what a change has to"
                            + " touch");
        }
    }
}
