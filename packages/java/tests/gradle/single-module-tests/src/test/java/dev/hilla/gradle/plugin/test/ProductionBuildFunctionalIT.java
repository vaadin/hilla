package dev.hilla.gradle.plugin.test;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.hilla.engine.commandrunner.GradleRunner;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * These tests are semantically unit tests, but since a production build
 * takes a while to complete, they are performed as ITs, which makes it
 * also easier to keep the package-lock.json file updated.
 * <p>
 * NOTE:
 * This test is working on the output of production mode build of single-module.
 */
public class ProductionBuildFunctionalIT {

    /**
     * Building a project in production mode can take a while,
     * so it is more efficient to check everything in one run.
     */
    @Test
    public void validateProductionBuildOutput() throws IOException {
        afterProductionBuild_openApiJson_hasCorrectEndpoints();
        afterProductionBuild_endpointsTs_hasCorrectEndpoints();
        afterProductionBuild_jarArchiveIsCreated();
    }

    private void afterProductionBuild_openApiJson_hasCorrectEndpoints() throws IOException {
        var openApiJsonPath = getBuildDirPath().resolve("classes/dev/hilla/openapi.json").toFile();
        var openApiJson = Json.mapper().readValue(openApiJsonPath, OpenAPI.class);
        assertTrue(openApiJson.getPaths().containsKey("/HelloReactEndpoint/sayHello"),
            "After production build openApi.json should contain '/HelloReactEndpoint/sayHello' path.");
    }

    private void afterProductionBuild_endpointsTs_hasCorrectEndpoints() throws IOException {
        var endpointTsPath = getFrontendGeneratedPath()
            .resolve("endpoints.ts");
        var endpointTsContent = String.join("", Files.readAllLines(endpointTsPath));
        assertTrue(endpointTsContent.contains("import * as HelloReactEndpoint"),
            "After production build endpoints.ts should contain 'import * as HelloReactEndpoint'.");
    }

    private void afterProductionBuild_jarArchiveIsCreated() {
        var executableJarFile = getBuildDirPath().resolve("libs/single-module.jar").toFile();
        assertTrue(executableJarFile.exists(),
            "After production build project output jar file with name 'single-module.jar' should exist under 'single-module/build/libs/'");
    }

    @BeforeEach
    public void runProductionBuild() {
        try {
            cleanPreviousBuildAndFrontendGeneratedFolders();
        } catch (IOException e) {
            fail(e);
        }
        runGradleCommand("--info -Philla.productionMode=true build");
    }

    private void runGradleCommand(String executable) {
        try {
            var projectRootDir = getProjectRootPath().toFile();
            var gradleRunner = GradleRunner.forProject(projectRootDir, executable.split("\\s+"))
                .orElseThrow();
            gradleRunner.run(null);
        } catch (Throwable t) {
            fail(t);
        }
    }

    private void cleanPreviousBuildAndFrontendGeneratedFolders() throws IOException {
        var buildDir = getBuildDirPath().toFile();
        if (buildDir.exists()) {
            FileUtils.deleteDirectory(buildDir);
        }
        var frontendGeneratedDir = getFrontendGeneratedPath().toFile();
        if (frontendGeneratedDir.exists()) {
            FileUtils.deleteDirectory(frontendGeneratedDir);
        }
    }

    private Path getProjectRootPath() {
        try {
            return Path.of(Objects.requireNonNull(
                            getClass().getClassLoader().getResource(""))
                        .toURI()) // test
                    .getParent()  // java
                    .getParent()  // classes
                    .getParent()  // build
                    .getParent()  // single-module-test
                    .getParent()  // gradle
                    .resolve("single-module");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getBuildDirPath() {
        return getProjectRootPath().resolve("build");
    }

    private Path getFrontendGeneratedPath() {
        return getProjectRootPath().resolve("frontend/generated");
    }
}
