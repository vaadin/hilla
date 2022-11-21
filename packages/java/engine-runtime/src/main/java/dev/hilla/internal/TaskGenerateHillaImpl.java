package dev.hilla.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

@NpmPackage(value = "@hilla/generator-typescript-core", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "2.0.0-alpha4")
@NpmPackage(value = "@hilla/generator-typescript-plugin-push", version = "2.0.0-alpha4")
public class TaskGenerateHillaImpl implements TaskGenerateHilla {
    static final boolean IS_WINDOWS;
    static final String MAVEN_COMMAND;

    static {
        var osName = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osName.contains("windows");
        MAVEN_COMMAND = IS_WINDOWS ? "mvn.cmd" : "mvn";
    }

    private File projectDirectory;

    @Override
    public void configure(File projectDirectory, String buildDirectoryName) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        var command = prepareCommand();
        runCodeGeneration(command);
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
