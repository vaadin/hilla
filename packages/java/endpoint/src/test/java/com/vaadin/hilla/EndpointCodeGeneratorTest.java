/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * Covers what a change of the browser callable classes in a running application
 * leads to: the TypeScript written again and the endpoints registered again.
 */
public class EndpointCodeGeneratorTest {
    @Rule
    public TemporaryFolder projectFolder = new TemporaryFolder();

    @After
    public void forgetTheApplicationContext() {
        new ApplicationContextProvider().setApplicationContext(null);
    }

    @Test
    public void should_WriteTheTypeScriptAndRegisterTheEndpointsAgain()
            throws IOException {
        var frontend = projectFolder.newFolder("src", "main", "frontend");
        var configuration = mock(ApplicationConfiguration.class);
        when(configuration.isProductionMode()).thenReturn(false);
        when(configuration.getProjectFolder())
                .thenReturn(projectFolder.getRoot());
        when(configuration.getBuildFolder()).thenReturn("target");
        when(configuration.getFrontendFolder()).thenReturn(frontend);

        var context = mock(VaadinContext.class);
        when(context.getAttribute(eq(ApplicationConfiguration.class), any()))
                .thenReturn(configuration);

        var applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(any()))
                .thenReturn(Map.of());
        when(applicationContext.getBeansWithAnnotation(BrowserCallable.class))
                .thenReturn(Map.of("greeting", new GreetingService()));
        new ApplicationContextProvider()
                .setApplicationContext(applicationContext);

        var endpointController = mock(EndpointController.class);

        new EndpointCodeGenerator(context, endpointController).update();

        var written = frontend.toPath().resolve("generated/GreetingService.ts");
        assertTrue("The TypeScript of the endpoint is written",
                Files.isRegularFile(written));
        assertTrue(Files.readString(written).contains(
                "client.call('GreetingService', 'greet', { name }, init)"));
        verify(endpointController).registerEndpoints();
    }

    @BrowserCallable
    public static class GreetingService {
        public String greet(String name) {
            return "Hello " + name;
        }
    }
}
