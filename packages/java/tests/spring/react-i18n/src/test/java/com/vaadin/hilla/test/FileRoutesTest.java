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
package com.vaadin.hilla.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileRoutesTest {

    private String fileRoutes;

    @Before
    public void loadChunks() throws IOException {
        var baseDir = new File(System.getProperty("user.dir", ".")).toPath();
        var fileRoutesPath = baseDir.resolve(Path.of("src", "main", "frontend",
                "generated", "file-routes.ts"));
        fileRoutes = Files.readString(fileRoutesPath);
    }

    @Test
    public void shouldHaveNonLazyIndex() {
        Assert.assertFalse(
                "Unexpected dynamic import for @index.tsx in generated/file-routes.ts",
                fileRoutes.contains("import(\"../views/@index.js\")"));
        Assert.assertTrue(
                "Expected static import for @index.tsx in generated/file-routes.ts",
                fileRoutes.contains("from \"../views/@index.js\";"));
    }

}
