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
package com.vaadin.hilla;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mockito.Mockito;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

@Component
public class ServletContextTestSetup implements ServletContextAware {

    private FeatureFlagCondition featureFlagCondition;

    @Override
    public void setServletContext(ServletContext servletContext) {
        Lookup lookup = Mockito.mock(Lookup.class);
        servletContext.setAttribute(Lookup.class.getName(), lookup);
        ApplicationConfiguration applicationConfiguration = Mockito
                .mock(ApplicationConfiguration.class);
        servletContext.setAttribute(ApplicationConfiguration.class.getName(),
                applicationConfiguration);
        featureFlagCondition = Mockito.mock(FeatureFlagCondition.class);
        Mockito.when(featureFlagCondition.matches(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        try {
            Path tmpDir = Files.createTempDirectory("test");
            Path target = tmpDir.resolve("target");
            target.toFile().mkdir();
            Mockito.when(applicationConfiguration.getProjectFolder())
                    .thenReturn(tmpDir.toFile());

            Mockito.when(applicationConfiguration.getBuildFolder())
                    .thenReturn(target.getFileName().toString());
            Mockito.when(applicationConfiguration
                    .getStringProperty(Mockito.anyString(), Mockito.any()))
                    .thenAnswer(q -> {
                        return q.getArgument(1); // The default value
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
