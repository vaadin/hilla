package com.vaadin.hilla.internal;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.hilla.engine.EngineConfiguration;

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

        EngineConfiguration.reset();
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
        return getTemporaryDirectory().resolve(getClassesDirectory())
                .resolve(EngineConfiguration.OPEN_API_PATH);
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

    protected EngineConfiguration getEngineConfiguration() {
        return new EngineConfiguration() {

            @Override
            public Path getBaseDir() {
                return getTemporaryDirectory();
            }

            @Override
            public Path getBuildDir() {
                return getBaseDir().resolve(getBuildDirectory());
            }

            @Override
            public List<Path> getClassesDirs() {
                return List.of(getBaseDir().resolve(getClassesDirectory()));
            }

            @Override
            public Path getOutputDir() {
                return getBaseDir().resolve(getOutputDirectory());
            }

            @Override
            public Path getOpenAPIFile() {
                return TaskTest.this.getOpenAPIFile();
            }
        };
    }

    protected OpenAPI getGeneratedOpenAPI() {
        return new OpenAPIV3Parser()
                .read(getOpenAPIFile().toFile().getAbsolutePath());
    }
}
