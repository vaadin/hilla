package dev.hilla.internal;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_GENERATED_DIR;

import java.io.IOException;
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

import dev.hilla.engine.EngineConfiguration;

public class TaskTest {
    private Path temporaryDirectory;

    @BeforeEach
    public void setUpTaskApplication() throws IOException, URISyntaxException,
            FrontendUtils.CommandExecutionException, InterruptedException {
        temporaryDirectory = Files.createTempDirectory(getClass().getName());
        temporaryDirectory.toFile().deleteOnExit();
        var userDir = temporaryDirectory.toAbsolutePath().toString();
        System.setProperty("user.dir", userDir);
        System.clearProperty(PARAM_FRONTEND_DIR);
        System.clearProperty(PARAM_GENERATED_DIR);

        var buildDir = getTemporaryDirectory().resolve(getBuildDirectory());
        Files.createDirectories(buildDir);

        var classPath = new LinkedHashSet(List
                .of(Path.of(getClass().getClassLoader().getResource("").toURI())
                        .toString()));
        // Create hilla-engine-configuration.json from template
        var configPath = buildDir.resolve(EngineConfiguration.RESOURCE_NAME);
        Files.copy(Path.of(Objects
                .requireNonNull(getClass()
                        .getResource(EngineConfiguration.RESOURCE_NAME))
                .toURI()), configPath);
        var config = EngineConfiguration.load(buildDir.toFile());
        // Modify runtime settings (base, class path) and save
        // hilla-engine-configuration.json
        Files.delete(configPath);
        config.setBaseDir(temporaryDirectory);
        config.setClassPath(classPath);
        config.store(buildDir.toFile());

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
                "install", "--no-save");

        var generatedFiles = Files
                .list(packagesDirectory).filter(dirName -> dirName.getFileName()
                        .toString().startsWith("generator-"))
                .map(Path::toString);

        var command = Stream.of(shellCmd, npmCmd, generatedFiles)
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

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    protected String getBuildDirectory() {
        return "build";
    }

    protected String getClassesDirectory() {
        return "build/classes";
    }

    protected String getOutputDirectory() {
        return "output";
    }

    protected Path getOpenAPIFile() {
        return getTemporaryDirectory().resolve(getClassesDirectory())
                .resolve(EngineConfiguration.OPEN_API_PATH);
    }
}
