package dev.hilla.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

@NpmPackage(value = "@hilla/generator-typescript-core", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "1.2.0-alpha3")
@NpmPackage(value = "@hilla/generator-typescript-plugin-push", version = "1.2.0-alpha3")
public class TaskGenerateHillaImpl implements TaskGenerateHilla {
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
        return List.of("mvn", "engine:generate");
    }

    List<String> prepareGradleCommand() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

}
