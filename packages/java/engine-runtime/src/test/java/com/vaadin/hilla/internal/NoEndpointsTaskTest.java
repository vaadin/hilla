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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.server.frontend.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.generator.typescript.OutputFolder;

@SpringBootTest(classes = {
        NoEndpointsTaskTest.NoopApplicationContextProvider.class })
public class NoEndpointsTaskTest extends TaskTest {
    private TaskGenerateOpenAPI taskGenerateTypeScript;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void should_GenerateEmptySchema_when_NoEndpointsFound()
            throws ExecutionFailedException, IOException, URISyntaxException {
        // Mock ApplicationContextProvider static API to prevent interference
        // with other tests.
        try (var mockApplicationContextProvider = Mockito
                .mockStatic(ApplicationContextProvider.class)) {
            mockApplicationContextProvider
                    .when(ApplicationContextProvider::getApplicationContext)
                    .thenReturn(applicationContext);
            mockApplicationContextProvider
                    .when(() -> ApplicationContextProvider
                            .runOnContext(Mockito.any()))
                    .thenAnswer(invocationOnMock -> {
                        invocationOnMock
                                .<Consumer<ApplicationContext>> getArgument(0)
                                .accept(applicationContext);
                        return null;
                    });

            // Create files resembling output for previously existing endpoints
            var outputDirectory = Files.createDirectory(
                    getTemporaryDirectory().resolve(getOutputDirectory()));
            var generatedFileListPath = outputDirectory
                    .resolve(OutputFolder.FILE_LIST);
            var referenceFileListPath = Path.of(Objects
                    .requireNonNull(
                            getClass().getResource(OutputFolder.FILE_LIST))
                    .toURI());
            Files.copy(referenceFileListPath, generatedFileListPath);
            var referenceFileList = Files.readAllLines(referenceFileListPath);
            for (String line : referenceFileList) {
                var path = outputDirectory.resolve(line);
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            var arbitraryGeneratedFile = outputDirectory.resolve("vaadin.ts");
            Files.createFile(arbitraryGeneratedFile);

            taskGenerateTypeScript = new TaskGenerateTypeScriptImpl(
                    getEngineConfiguration());

            assertDoesNotThrow(taskGenerateTypeScript::execute,
                    "Expected to not fail without npm dependencies");

            assertFalse(generatedFileListPath.toFile().exists(),
                    "Expected file list to be deleted");
            for (String line : referenceFileList) {
                var path = outputDirectory.resolve(line);
                assertFalse(path.toFile().exists(),
                        String.format("Expected file %s to be deleted", path));
            }
            assertTrue(arbitraryGeneratedFile.toFile().exists(),
                    "Expected non-Hilla generated file to not be deleted");
        }
    }

    static class NoopApplicationContextProvider
            extends ApplicationContextProvider {
        @Override
        public void setApplicationContext(
                @Nonnull ApplicationContext applicationContext)
                throws BeansException {
            // do nothing
        }
    }
}
