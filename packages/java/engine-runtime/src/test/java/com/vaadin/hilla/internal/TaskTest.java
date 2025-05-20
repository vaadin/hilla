package com.vaadin.hilla.internal;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.server.frontend.FrontendUtils;
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
