package com.vaadin.hilla.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.loader.tools.MainClassFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class to find endpoints in a non-running Hilla application.
 */
public class AotEndpointProvider {
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private final EngineConfiguration engineConfiguration;

    public AotEndpointProvider(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public List<Class<?>> findEndpointClasses()
            throws IOException, InterruptedException {
        // Prepares all variables based on the provided configuration
        var aotOutput = engineConfiguration.getBuildDir()
                .resolve("spring-aot/main");
        var classesDirectory = aotOutput.resolve("classes");
        var applicationClass = (engineConfiguration.getMainClass() != null)
                ? engineConfiguration.getMainClass()
                : findSingleClass(engineConfiguration.getBuildDir()
                        .resolve("classes").toFile());
        var settings = List.of(applicationClass,
                aotOutput.resolve("sources").toString(),
                aotOutput.resolve("resources").toString(),
                classesDirectory.toString(), engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId());
        var javaExecutable = ProcessHandle.current().info().command()
                .orElse(Path.of(System.getProperty("java.home"), "bin", "java")
                        .toString());

        // Runs the SpringApplicationAotProcessor to generate the
        // reflect-config.json file. This comes from the `process-aot` goal.
        var processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        processBuilder.command(javaExecutable, "-cp",
                engineConfiguration.getClasspath().stream().map(Path::toString)
                        .collect(Collectors.joining(File.pathSeparator)),
                "org.springframework.boot.SpringApplicationAotProcessor");
        processBuilder.command().addAll(settings);

        Process process = processBuilder.start();
        process.waitFor();

        var json = aotOutput.resolve(Path.of("resources", "META-INF",
                "native-image", engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId(), "reflect-config.json"));

        if (!Files.isRegularFile(json)) {
            throw new ParserException("Aot file reflect-config.json not found");
        }

        // The file simply contains a list of beans, we just need their names,
        // which are class names.
        String jsonContent = Files.readString(json);
        var objectMapper = new ObjectMapper();
        var rootNode = objectMapper.readTree(jsonContent);

        if (rootNode.isArray()) {
            var candidates = new ArrayList<String>();

            for (var node : rootNode) {
                String name = node.get("name").asText();
                candidates.add(name);
            }

            return candidates.stream().map(name -> {
                try {
                    return Class.forName(name);
                }
                // Must also catch NoClassDefFoundError here, exceptions are not
                // enough.
                catch (Throwable t) {
                    return null;
                }
            }).filter(Objects::nonNull)
                    // Filter out classes that are not annotated with any of the
                    // endpoint annotations.
                    .filter(cls -> engineConfiguration.getParser()
                            .getEndpointAnnotations().stream()
                            .anyMatch(cls::isAnnotationPresent))
                    .collect(Collectors.toList());
        }

        throw new ParserException("No endpoints detected");
    }

    static String findSingleClass(File classesDirectory) throws IOException {
        String mainClass = MainClassFinder.findSingleMainClass(classesDirectory,
                SPRING_BOOT_APPLICATION_CLASS_NAME);
        if (mainClass != null) {
            return mainClass;
        }
        throw new ParserException("Failed to find a single main class");
    }
}
