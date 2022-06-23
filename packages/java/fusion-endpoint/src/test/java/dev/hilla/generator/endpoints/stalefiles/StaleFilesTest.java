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

package dev.hilla.generator.endpoints.stalefiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

import dev.hilla.generator.endpoints.AbstractEndpointGenerationTest;

public class StaleFilesTest extends AbstractEndpointGenerationTest {
    static private final String PACKAGE_PATH = StaleFilesTest.class.getPackage()
            .getName().replace(".", "/");

    public StaleFilesTest() {
        super(Arrays.asList(NewEndpoint.class));
    }

    @Test
    public void should_RemoveStaleGeneratedFiles() throws IOException {
        for (final File priorExistingFile : filesInOutputDirectory(
                // Theme generated files
                "theme.js", "theme.d.ts", "theme-test-case.generated.js",
                // Old endpoints
                "OldUserEndpoint.ts",
                // Old endpoints’ TypeScript interfaces and models
                "org/example/domain/OldUser.ts",
                "org/example/domain/OldUserModel.ts",
                "org/example/common/OldEntity.ts",
                "org/example/common/OldEntityModel.ts")) {
            priorExistingFile.getParentFile().mkdirs();
            priorExistingFile.createNewFile();
        }

        generateOpenApi(null);
        generateTsEndpoints();

        for (final File remainingFile : filesInOutputDirectory(
                // Theme files
                "theme.js", "theme.d.ts", "theme-test-case.generated.js",
                // New endpoint
                "NewEndpoint.ts",
                // New endpoint’s data definition
                PACKAGE_PATH + "/NewEndpoint/Account.ts",
                // New endpoint’s form model
                PACKAGE_PATH + "/NewEndpoint/AccountModel.ts")) {
            Assert.assertTrue(
                    String.format("Expected file '%s' to exist", remainingFile),
                    remainingFile.exists());
        }

        for (final File deletedFile : filesInOutputDirectory(
                // Old endpoints
                "OldEndpoint.ts",
                // Old endpoints’ TypeScript interfaces and models
                "org/example/domain/OldUser.ts",
                "org/example/domain/OldUserModel.ts",
                "org/example/common/OldEntity.ts",
                "org/example/common/OldEntityModel.ts",
                // Emptied directories
                "org/example/domain", "org/example/common", "org/example",
                "org")) {
            Assert.assertFalse(String.format("Expected file '%s' to not exist",
                    deletedFile), deletedFile.exists());
        }

    }

    private List<File> filesInOutputDirectory(String... paths) {
        return Arrays.stream(paths).map(FilenameUtils::separatorsToSystem)
                .map(pathname -> new File(outputDirectory.getRoot(), pathname))
                .collect(Collectors.toList());
    }
}
