package dev.hilla.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

@NpmPackage(value = "@hilla/generator-typescript-core", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "1.1.0")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "1.1.0")
public class TaskGenerateHillaImpl implements TaskGenerateHilla {

    @Override
    public void execute() throws ExecutionFailedException {
        var baseDir = System.getProperty("user.dir", ".");
        var command = prepareCommand(baseDir);
        runCodeGeneration(command);
    }

    void runCodeGeneration(List<String> command)
            throws ExecutionFailedException {
        var exitCode = 0;
        try {
            ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
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

    List<String> prepareCommand(String baseDir) {
        var path = Paths.get(baseDir);
        if (path.toFile().isDirectory()) {
            if (isMavenProject(path)) {
                return prepareMavenCommand();
            } else if (isGradleProject(path)) {
                return prepareGradleCommand();
            }
        }
        throw new IllegalStateException(String
                .format("Failed to determine project directory for dev mode. "
                        + "Directory '%s' does not look like a Maven or "
                        + "Gradle project.", path.toString()));
    }

    List<String> prepareMavenCommand() {
        return List.of("mvn", "generator:generate");
    }

    List<String> prepareGradleCommand() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

}
