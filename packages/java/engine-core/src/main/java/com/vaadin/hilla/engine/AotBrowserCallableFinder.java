package com.vaadin.hilla.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.loader.tools.MainClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
 * Utility class to find browser callables (endpoints) in a non-running Hilla
 * application.
 */
class AotBrowserCallableFinder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AotBrowserCallableFinder.class);
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String SPRING_AOT_PROCESSOR = "org.springframework.boot.SpringApplicationAotProcessor";

    static List<Class<?>> findEndpointClasses(
            EngineConfiguration engineConfiguration)
            throws IOException, InterruptedException {
        // Determine the main application class
        var applicationClass = determineApplicationClass(engineConfiguration);
        if (applicationClass == null) {
            LOGGER.warn("This project has not been recognized as a Spring Boot"
                    + " application because a main class could not be found."
                    + " Hilla services will not be available.");
            return List.of();
        }

        // Generate the AOT artifacts, including reflect-config.json
        var reflectConfigPath = generateAotArtifacts(engineConfiguration,
                applicationClass);

        // Load annotated classes from reflect-config.json
        return loadAnnotatedClasses(engineConfiguration, reflectConfigPath);
    }

    private static String determineApplicationClass(
            EngineConfiguration engineConfiguration) throws IOException {
        var mainClass = engineConfiguration.getMainClass();
        if (mainClass != null) {
            return mainClass;
        }

        return MainClassFinder.findSingleMainClass(
                engineConfiguration.getClassesDir().toFile(),
                SPRING_BOOT_APPLICATION_CLASS_NAME);
    }

    private static Path generateAotArtifacts(
            EngineConfiguration engineConfiguration, String applicationClass)
            throws IOException, InterruptedException {
        var aotOutput = engineConfiguration.getBuildDir()
                .resolve("spring-aot/main");
        var classesDirectory = aotOutput.resolve("classes");
        var classpath = engineConfiguration.getClasspath().stream()
                .filter(Files::exists).toList();

        var settings = Stream.of("-cp",
                classpath.stream().map(AotBrowserCallableFinder::quotePath)
                        .collect(Collectors.joining(File.pathSeparator)),
                SPRING_AOT_PROCESSOR, applicationClass,
                quotePath(aotOutput.resolve("sources")),
                quotePath(aotOutput.resolve("resources")),
                quotePath(classesDirectory), engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId()).toList();

        var argsFile = engineConfiguration.getBuildDir()
                .resolve("hilla-aot-args.txt");
        Files.write(argsFile, settings);

        var javaExecutable = ProcessHandle.current().info().command()
                .orElse(Path.of(System.getProperty("java.home"), "bin", "java")
                        .toString());

        // Runs the SpringApplicationAotProcessor to generate the
        // reflect-config.json file. This comes from the `process-aot` goal.
        int exitCode = new ProcessBuilder().inheritIO()
                .command(javaExecutable, "@" + argsFile).start().waitFor();

        if (exitCode != 0) {
            LOGGER.error(
                    SPRING_AOT_PROCESSOR + " exited with code: " + exitCode);
        }

        var json = aotOutput.resolve(Path.of("resources", "META-INF",
                "native-image", engineConfiguration.getGroupId(),
                engineConfiguration.getArtifactId(), "reflect-config.json"));

        if (!Files.isRegularFile(json)) {
            throw new ParserException(String.format(
                    "The `%s` tool has not produced the expected"
                            + " `reflect-config.json` file, which is used to"
                            + " identify available endpoints.",
                    SPRING_AOT_PROCESSOR));
        }

        return json;
    }

    private static List<Class<?>> loadAnnotatedClasses(
            EngineConfiguration engineConfiguration, Path reflectConfigPath)
            throws IOException {
        // The file simply contains a list of beans, we just need their names,
        // which are class names.
        var jsonContent = Files.readString(reflectConfigPath);
        var objectMapper = new ObjectMapper();
        var rootNode = objectMapper.readTree(jsonContent);

        if (!rootNode.isArray()) {
            throw new ParserException(
                    "Aot output file reflect-config.json does not contain"
                            + " information about beans, so endpoint detection"
                            + " cannot be performed");
        }

        // Extract candidate class names
        var candidates = new ArrayList<String>();
        for (var node : rootNode) {
            String name = node.get("name").asText();
            candidates.add(name);
        }

        // Prepare classloader
        var classpath = engineConfiguration.getClasspath().stream()
                .filter(Files::exists).toList();

        var urls = classpath.stream().map(Path::toFile).map(file -> {
            try {
                return file.toURI().toURL();
            } catch (Throwable t) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(URL[]::new);

        var annotationNames = engineConfiguration.getParser()
                .getEndpointAnnotations().stream().map(Class::getName).toList();

        var classLoader = new URLClassLoader(urls,
                AotBrowserCallableFinder.class.getClassLoader());
        return candidates.stream().map(name -> {
            try {
                return Class.forName(name, false, classLoader);
            } catch (Throwable t) {
                return null;
            }
        }).filter(Objects::nonNull)
                .filter(cls -> Arrays.stream(cls.getAnnotations())
                        .map(Annotation::annotationType).map(Class::getName)
                        .anyMatch(annotationNames::contains))
                .collect(Collectors.toList());
    }

    private static String quotePath(Path path) {
        return '"' + path.toString().replace("\\", "\\\\") + '"';
    }
}
