package dev.hilla.gradle.plugin.test;


import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import dev.hilla.engine.commandrunner.CommandNotFoundException;
import dev.hilla.engine.commandrunner.CommandRunnerException;

import static org.junit.jupiter.api.Assertions.*;

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
    public void runProductionBuild() throws CommandRunnerException {
        try {
            cleanPreviousBuildAndFrontendGeneratedFolders();
        } catch (IOException e) {
            fail(e);
        }
        runGradleCommand("./gradlew -Pvaadin.productionMode=true build");
    }

    private void runGradleCommand(String executable) throws CommandRunnerException {

        var exitCode = 0;
        var projectRootDir = getProjectRootPath().toFile();
        try {
            var builder = new ProcessBuilder(executable.split("\\s+"))
                .directory(projectRootDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
            var process = builder.start();

            exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(
                    String.format("Process exit code for executing %s is: %d", executable, exitCode));
            }
        } catch (IOException | InterruptedException e) {
            // Tries to figure out if the command is not found. This is not a
            // 100% reliable way to do it, but an exception will be thrown
            // anyway.
            if (e.getCause() != null && e.getCause().getMessage() != null
                && e.getCause().getMessage()
                .contains("No such file or directory")) {
                throw new CommandNotFoundException(String.format(
                    "Command or file not found in %s: %s", projectRootDir.getPath(), executable), e);
            }

            throw new CommandRunnerException(
                "Failed to execute command: " + executable, e);
        } catch (RuntimeException e) {
            fail(e);
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
