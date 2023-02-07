package dev.hilla.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendToolsSettings;
import com.vaadin.flow.server.frontend.FrontendUtils;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_GENERATED_DIR;

public class TaskTest {
    private Path temporaryDirectory;

    private String userDir;

    @BeforeEach
    public void setUpTaskApplication() throws IOException, URISyntaxException,
            FrontendUtils.CommandExecutionException, InterruptedException {
        temporaryDirectory = Files.createTempDirectory(getClass().getName());
        temporaryDirectory.toFile().deleteOnExit();
        userDir = temporaryDirectory.toAbsolutePath().toString();
        System.setProperty("user.dir", userDir);
        System.clearProperty(PARAM_FRONTEND_DIR);
        System.clearProperty(PARAM_GENERATED_DIR);

        Files.createFile(getApplicationPropertiesFile());

        var buildDir = getTemporaryDirectory().resolve(getBuildDirectory());
        Files.createDirectories(buildDir);

        var classPath = new LinkedHashSet(List
                .of(Path.of(getClass().getClassLoader().getResource("").toURI())
                        .toString()));
        // create hilla-engine-configuration.json
        var config = new EngineConfiguration();
        config.setBaseDir(temporaryDirectory);
        config.setBuildDir(getBuildDirectory());
        config.setClassPath(classPath);
        var parserConfig = new ParserConfiguration();
        parserConfig.setEndpointAnnotation(Endpoint.class.getName());
        parserConfig
                .setEndpointExposedAnnotation(EndpointExposed.class.getName());
        config.setParser(parserConfig);
        config.setGenerator(new GeneratorConfiguration());
        config.store(buildDir.toFile());

        var nodeModulesDirectory = getTemporaryDirectory()
                .resolve("node_modules");
        var packagesDirectory = Path
                .of(getClass().getClassLoader().getResource("").toURI())
                .getParent() // target
                .getParent() // engine-runtime
                .getParent() // java
                .getParent() // packages
                .resolve("ts");
        var generatorPackages = Files
                .list(packagesDirectory).filter(dirName -> dirName.getFileName()
                        .toString().startsWith("generator-"))
                .map(Path::toString).toList();

        var command = new ArrayList<>(List.of("npm", "--no-update-notifier",
                "--no-audit", "install", "--no-save"));
        command.addAll(generatorPackages);
        var processBuilder = FrontendUtils.createProcessBuilder(command);
        processBuilder.directory(temporaryDirectory.toFile());
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        var exitCode = processBuilder.start().waitFor();
        if (exitCode != 0) {
            throw new FrontendUtils.CommandExecutionException(exitCode);
        }
    }

    @AfterEach
    public void tearDownTaskApplication() throws IOException {
        FrontendUtils.deleteDirectory(temporaryDirectory.toFile());
    }

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    protected String getBuildDirectory() {
        return "build";
    }

    protected Path getApplicationPropertiesFile() {
        return temporaryDirectory.resolve("application.properties");
    }
}
