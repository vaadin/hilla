package com.vaadin.hilla.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
