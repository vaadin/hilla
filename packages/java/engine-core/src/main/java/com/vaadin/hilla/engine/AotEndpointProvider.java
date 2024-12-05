package com.vaadin.hilla.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.loader.tools.MainClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to find endpoints in a non-running Hilla application.
 */
public class AotEndpointProvider {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AotEndpointProvider.class);
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
                : MainClassFinder.findSingleMainClass(
                        engineConfiguration.getClassesDir().toFile(),
                        SPRING_BOOT_APPLICATION_CLASS_NAME);

        if (applicationClass == null) {
            LOGGER.warn("This project has not been recognized as a Spring Boot"
                    + " application because a main class could not be found."
                    + " Hilla services will not be available.");
            return List.of();
        }

        var classpath = engineConfiguration.getClasspath().stream()
                .filter(Files::exists).map(Path::toString).toList();
        var settings = Stream.of("-cp",
                classpath.stream()
                        .collect(Collectors.joining(File.pathSeparator)),
                "org.springframework.boot.SpringApplicationAotProcessor",
                applicationClass, aotOutput.resolve("sources"),
                aotOutput.resolve("resources"), classesDirectory,
                engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId()).map(Object::toString)
                .map(s -> '"' + s.replace("\\", "\\\\") + '"').toList();
        var argsFile = engineConfiguration.getBuildDir()
                .resolve("aot-args-" + System.currentTimeMillis() + ".txt");
        Files.write(argsFile, settings);
        var javaExecutable = ProcessHandle.current().info().command()
                .orElse(Path.of(System.getProperty("java.home"), "bin", "java")
                        .toString());

        // Runs the SpringApplicationAotProcessor to generate the
        // reflect-config.json file. This comes from the `process-aot` goal.
        var processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        processBuilder.command(javaExecutable, "@" + argsFile);

        var process = processBuilder.start();
        var exitCode = process.waitFor();

        if (exitCode != 0) {
            try (var reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                var errorMessage = reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                throw new ParserException("SpringApplicationAotProcessor failed with exit code "
                        + exitCode + ": " + errorMessage);
            }
        }

        Files.delete(argsFile);

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

            var urls = classpath.stream().map(File::new).map(file -> {
                try {
                    return file.toURI().toURL();
                } catch (Throwable t) {
                    return null;
                }
            }).filter(Objects::nonNull).toArray(URL[]::new);

            var annotationNames = engineConfiguration.getParser()
                    .getEndpointAnnotations().stream().map(Class::getName)
                    .toList();

            try (var classLoader = new URLClassLoader(urls,
                    AotEndpointProvider.class.getClassLoader())) {
                return candidates.stream().map(name -> {
                    try {
                        return Class.forName(name, false, classLoader);
                    }
                    // Must also catch NoClassDefFoundError here, exceptions are
                    // not enough.
                    catch (Throwable t) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                        // Filter out classes that are not annotated with any of
                        // the endpoint annotations.
                        .filter(cls -> Arrays.stream(cls.getAnnotations())
                                .map(Annotation::annotationType)
                                .map(Class::getName)
                                .anyMatch(annotationNames::contains))
                        .collect(Collectors.toList());
            }
        }

        throw new ParserException("No endpoints detected");
    }
}
