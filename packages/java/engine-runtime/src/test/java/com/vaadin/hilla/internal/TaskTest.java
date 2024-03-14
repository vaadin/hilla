package com.vaadin.hilla.internal;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.parser.testutils.TestEngineConfigurationPathResolver;

public class TaskTest {
    private Path temporaryDirectory;

    @BeforeEach
    public void setUpTaskApplication() throws IOException, URISyntaxException,
            FrontendUtils.CommandExecutionException, InterruptedException,
            InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            NoSuchFieldException {
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

        // Create hilla-engine-configuration.json from template
        var configPath = buildDir
                .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME);
        Files.copy(
                Path.of(Objects
                        .requireNonNull(getClass().getResource(
                                EngineConfiguration.DEFAULT_CONFIG_FILE_NAME))
                        .toURI()),
                configPath);

        var config = prepareConfiguration(buildDir);

        Files.delete(configPath);
        config.store(configPath.toFile());

        // Let Hilla know that the file has been generated
        var field = AbstractTaskEndpointGenerator.class
                .getDeclaredField("firstRun");
        field.setAccessible(true);
        field.set(null, false);

        var packagesDirectory = Path
                .of(getClass().getClassLoader().getResource("").toURI())
                .getParent() // target
                .getParent() // engine-runtime
                .getParent() // java
                .getParent() // packages
                .resolve("ts");

        var shellCmd = FrontendUtils.isWindows() ? Stream.of("cmd.exe", "/c")
                : Stream.<String> empty();

        var npmCmd = Stream.of("npm", "--no-update-notifier", "--no-audit",
                "install", "--no-save", "--install-links");

        var generatorFiles = Files.list(packagesDirectory).map(Path::toString);

        var command = Stream.of(shellCmd, npmCmd, generatorFiles)
                .flatMap(Function.identity()).toList();

        var processBuilder = FrontendUtils.createProcessBuilder(command)
                .directory(temporaryDirectory.toFile())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
        var exitCode = processBuilder.start().waitFor();
        if (exitCode != 0) {
            throw new FrontendUtils.CommandExecutionException(exitCode);
        }
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

    /**
     * Modifies runtime settings (paths, class path)
     */
    private EngineConfiguration prepareConfiguration(Path buildDir)
            throws URISyntaxException, IOException, InvocationTargetException,
            NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        var classPath = new LinkedHashSet<>(List
                .of(Path.of(getClass().getClassLoader().getResource("").toURI())
                        .toString()));

        var config = EngineConfiguration.loadDirectory(buildDir);

        config = TestEngineConfigurationPathResolver.resolve(config,
                temporaryDirectory);

        return new EngineConfiguration.Builder(config).classPath(classPath)
                .create();
    }
}
