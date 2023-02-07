package dev.hilla.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import dev.hilla.maven.runner.GeneratorUnavailableException;
import dev.hilla.maven.runner.PluginConfiguration;
import dev.hilla.maven.runner.PluginException;
import dev.hilla.maven.runner.PluginRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

@NpmPackage(value = "@hilla/generator-typescript-core", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-push", version = "2.0.0-beta1")
public class TaskGenerateHillaImpl implements TaskGenerateHilla {
    static final boolean IS_WINDOWS;
    static final String MAVEN_COMMAND;

    static {
        var osName = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osName.contains("windows");
        MAVEN_COMMAND = IS_WINDOWS ? "mvn.cmd" : "mvn";
    }

    private static final Logger logger = LoggerFactory
            .getLogger(TaskGenerateHillaImpl.class);

    private File projectDirectory;
    private String buildDirectoryName;

    @Override
    public void configure(File projectDirectory, String buildDirectoryName) {
        this.projectDirectory = projectDirectory;
        this.buildDirectoryName = buildDirectoryName;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        try {
            // the configure method should be called before execute
            if (projectDirectory == null) {
                throw new ExecutionFailedException("Project directory not set");
            }

            PluginConfiguration config = null;

            if (buildDirectoryName != null) {
                var buildDir = new File(projectDirectory, buildDirectoryName);

                try {
                    config = PluginConfiguration.load(buildDir);
                } catch (IOException e) {
                    logger.warn(
                            "Hilla Maven Plugin configuration found, but not read correctly",
                            e);
                }
            }

            if (config == null) {
                logger.info(
                        "Hilla Maven Plugin configuration not found: run generator using Maven");
                var command = prepareCommand();
                runCodeGeneration(command);
            } else {
                logger.info(
                        "Hilla Maven Plugin configuration found: run generator directly");
                new PluginRunner(config).execute();
            }
        } catch (PluginException | GeneratorUnavailableException e) {
            throw new ExecutionFailedException(e);
        }
    }

    void runCodeGeneration(List<String> command)
            throws ExecutionFailedException {
        var exitCode = 0;
        try {
            ProcessBuilder builder = new ProcessBuilder(command)
                    .directory(projectDirectory).inheritIO();
            exitCode = builder.start().waitFor();
        } catch (Exception e) {
            throw new ExecutionFailedException(
                    "Hilla Generator execution failed", e);
        }
        if (exitCode != 0) {
            throw new ExecutionFailedException(
                    "Hilla Generator execution failed with exit code "
                            + exitCode);
        }
    }

    boolean isMavenProject(Path path) {
        return path.resolve("pom.xml").toFile().exists();
    }

    private boolean isGradleProject(Path path) {
        return path.resolve("build.gradle").toFile().exists();
    }

    List<String> prepareCommand() {
        if (projectDirectory.isDirectory()) {
            var path = projectDirectory.toPath();
            if (isMavenProject(path)) {
                return prepareMavenCommand();
            } else if (isGradleProject(path)) {
                return prepareGradleCommand();
            }
        }
        throw new IllegalStateException(String
                .format("Failed to determine project directory for dev mode. "
                        + "Directory '%s' does not look like a Maven or "
                        + "Gradle project.", projectDirectory));
    }

    List<String> prepareMavenCommand() {
        return List.of(MAVEN_COMMAND, "hilla:generate");
    }

    List<String> prepareGradleCommand() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }
}
