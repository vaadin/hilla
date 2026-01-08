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
package com.vaadin.hilla.internal;

import static com.vaadin.flow.internal.FrontendUtils.PARAM_FRONTEND_DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.hilla.engine.EngineAutoConfiguration;

public class TaskTest {
    private Path temporaryDirectory;

    @BeforeEach
    public void setUpTaskApplication() throws IOException {
        temporaryDirectory = Files.createTempDirectory(getClass().getName());
        temporaryDirectory.toFile().deleteOnExit();
        var userDir = temporaryDirectory.toAbsolutePath().toString();
        System.setProperty("user.dir", userDir);
        System.clearProperty(PARAM_FRONTEND_DIR);

        var buildDir = getTemporaryDirectory().resolve(getBuildDirectory());
        Files.createDirectories(buildDir);

        var frontendDir = getTemporaryDirectory()
                .resolve(getFrontendDirectory());
        Files.createDirectories(frontendDir);
    }

    @AfterEach
    public void tearDownTaskApplication() throws IOException {
        FrontendUtils.deleteDirectory(temporaryDirectory.toFile());
    }

    protected String getBuildDirectory() {
        return "build";
    }

    protected String getClassesDirectory() {
        return "build/classes";
    }

    protected Path getOpenAPIFile() {
        return getTemporaryDirectory().resolve(getBuildDirectory())
                .resolve(EngineAutoConfiguration.OPEN_API_PATH);
    }

    protected String getFrontendDirectory() {
        return "frontend";
    }

    protected String getOutputDirectory() {
        return "output";
    }

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    protected EngineAutoConfiguration getEngineConfiguration() {
        return new EngineAutoConfiguration.Builder()
                .baseDir(getTemporaryDirectory()).buildDir(getBuildDirectory())
                .outputDir(getOutputDirectory()).withDefaultAnnotations()
                .build();
    }

    protected OpenAPI getGeneratedOpenAPI() {
        return new OpenAPIV3Parser()
                .read(getOpenAPIFile().toFile().getAbsolutePath());
    }
}
