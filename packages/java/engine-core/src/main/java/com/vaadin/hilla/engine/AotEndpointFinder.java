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

public class AotEndpointFinder {
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private final EngineConfiguration engineConfiguration;

    public AotEndpointFinder(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public List<Class<?>> findEndpointClasses()
            throws IOException, InterruptedException {
        var aotOutput = engineConfiguration.getBuildDir()
                .resolve("spring-aot/main");
        var classesDirectory = aotOutput.resolve("classes");
        var applicationClass = (EngineConfiguration.mainClass != null)
                ? EngineConfiguration.mainClass
                : findSingleClass(classesDirectory.toFile());
        var settings = List.of(applicationClass,
                aotOutput.resolve("sources").toString(),
                aotOutput.resolve("resources").toString(),
                classesDirectory.toString(), EngineConfiguration.groupId,
                EngineConfiguration.artifactId);
        var javaExecutable = ProcessHandle.current().info().command()
                .orElse(Path.of(System.getProperty("java.home"), "bin", "java")
                        .toString());
        var processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        processBuilder.command(javaExecutable, "-cp",
                EngineConfiguration.classpath,
                "org.springframework.boot.SpringApplicationAotProcessor");
        processBuilder.command().addAll(settings);

        Process process = processBuilder.start();
        process.waitFor();

        var json = aotOutput.resolve(Path.of("resources", "META-INF",
                "native-image", EngineConfiguration.groupId,
                EngineConfiguration.artifactId, "reflect-config.json"));

        if (!Files.isRegularFile(json)) {
            throw new ParserException("Aot file reflect-config.json not found");
        }

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
                // Must also catch NoClassDefFoundError
                catch (Throwable t) {
                    return null;
                }
            }).filter(Objects::nonNull)
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
