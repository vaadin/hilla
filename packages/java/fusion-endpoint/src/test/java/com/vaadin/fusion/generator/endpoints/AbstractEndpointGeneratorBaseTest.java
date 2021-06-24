package com.vaadin.fusion.generator.endpoints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.vaadin.fusion.generator.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.EndpointExposed;
import com.vaadin.fusion.generator.OpenApiConfiguration;
import com.vaadin.fusion.generator.OpenApiObjectGenerator;
import com.vaadin.fusion.generator.OpenApiSpecGenerator;
import com.vaadin.fusion.generator.VaadinConnectTsGenerator;

import elemental.json.Json;
import elemental.json.JsonObject;

public abstract class AbstractEndpointGeneratorBaseTest {

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();
    protected Path openApiJsonOutput;
    protected final List<Class<?>> endpointClasses = new ArrayList<>();
    protected final List<Class<?>> endpointExposedClasses = new ArrayList<>();
    protected final List<Class<?>> nonEndpointClasses = new ArrayList<>();
    protected final Package testPackage;

    public AbstractEndpointGeneratorBaseTest(List<Class<?>> testClasses) {
        testPackage = getClass().getPackage();
        collectEndpointClasses(endpointClasses, endpointExposedClasses,
                nonEndpointClasses, testClasses);
    }

    @Before
    public void setUpOutputFile() {
        openApiJsonOutput = java.nio.file.Paths.get(
                outputDirectory.getRoot().getAbsolutePath(), "openapi.json");
    }

    private void collectEndpointClasses(List<Class<?>> endpointClasses,
            List<Class<?>> endpointExposedClasses,
            List<Class<?>> nonEndpointClasses, List<Class<?>> inputClasses) {
        for (Class<?> testEndpointClass : inputClasses) {
            if (testEndpointClass.isAnnotationPresent(Endpoint.class)) {
                endpointClasses.add(testEndpointClass);
            } else if (testEndpointClass
                    .isAnnotationPresent(EndpointExposed.class)) {
                endpointExposedClasses.add(testEndpointClass);
            } else {
                nonEndpointClasses.add(testEndpointClass);
            }
            collectEndpointClasses(endpointClasses, endpointExposedClasses,
                    nonEndpointClasses,
                    Arrays.asList(testEndpointClass.getDeclaredClasses()));
        }
    }

    protected void generateTsEndpoints() {
        VaadinConnectTsGenerator.launch(openApiJsonOutput.toFile(),
                outputDirectory.getRoot());
    }

    protected void generateOpenApi(URL customApplicationProperties) {
        Properties applicationProperties = customApplicationProperties == null
                ? new Properties()
                : TestUtils
                        .readProperties(customApplicationProperties.getPath());
        new OpenApiSpecGenerator(applicationProperties).generateOpenApiSpec(
                Collections
                        .singletonList(java.nio.file.Paths.get("src/test/java",
                                testPackage.getName().replace('.',
                                        File.separatorChar))),
                openApiJsonOutput);
    }

    protected List<File> getTsFiles(File directory) {
        return Arrays.asList(
                directory.listFiles((dir, name) -> name.endsWith(".ts")));
    }

    protected String readFile(Path file) {
        try {
            return StringUtils.toEncodedString(Files.readAllBytes(file),
                    StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new AssertionError(
                    String.format("Failed to read the file '%s'", file));
        }
    }

    protected JsonObject readJsonFile(Path file) {
        return Json.parse(readFile(file));
    }

    protected OpenAPI getOpenApiObject() {
        OpenApiObjectGenerator generator = new OpenApiObjectGenerator();

        Path javaSourcePath = java.nio.file.Paths.get("src/test/java/",
                testPackage.getName().replace('.', File.separatorChar));
        generator.addSourcePath(javaSourcePath);

        generator.setOpenApiConfiguration(new OpenApiConfiguration("Test title",
                "0.0.1", "https://server.test", "Test description"));

        return generator.getOpenApi();
    }

}
