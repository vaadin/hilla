package com.vaadin.hilla.typescript.codegen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to ensure the TypeScript codegen layer does not depend on OpenAPI
 * classes. The codegen layer generates TypeScript directly from Java models
 * without using OpenAPI as an intermediate format.
 *
 * OpenAPI is only used by the Parser layer for its own output format.
 */
public class OpenAPIIsolationTest {

    @Test
    public void codegenLayerShouldNotImportOpenAPIClasses() throws IOException {
        Path codegenPath = Paths.get("src/main/java/com/vaadin/hilla/typescript/codegen");

        List<String> violatingFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(codegenPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(file -> {
                     try {
                         String content = Files.readString(file);

                         // Check for OpenAPI/Swagger imports
                         if (content.contains("import io.swagger") ||
                             content.contains("import org.openapitools")) {
                             violatingFiles.add(file.toString());
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }

        assertTrue(violatingFiles.isEmpty(),
                "The following codegen files import OpenAPI classes, which violates " +
                "the architectural boundary. Codegen should only use Java models " +
                "(ClassInfoModel, MethodInfoModel, etc.) directly:\n" +
                String.join("\n", violatingFiles));
    }
}
