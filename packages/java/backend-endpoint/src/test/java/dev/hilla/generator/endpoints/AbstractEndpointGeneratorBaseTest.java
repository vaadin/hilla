package dev.hilla.generator.endpoints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import dev.hilla.Endpoint;
import dev.hilla.EndpointExposed;
import dev.hilla.generator.MainGenerator;
import dev.hilla.generator.OpenAPIConfiguration;
import dev.hilla.generator.OpenAPIObjectGenerator;
import dev.hilla.generator.OpenAPISpecGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import dev.hilla.utils.TestUtils;

import elemental.json.Json;
import elemental.json.JsonObject;

public abstract class AbstractEndpointGeneratorBaseTest {

    protected final List<Class<?>> endpointClasses = new ArrayList<>();
    protected final List<Class<?>> endpointExposedClasses = new ArrayList<>();
    protected final List<Class<?>> nonEndpointClasses = new ArrayList<>();
    protected final Package testPackage;
    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();
    protected Path openApiJsonOutput;

    public AbstractEndpointGeneratorBaseTest(List<Class<?>> testClasses) {
        testPackage = getClass().getPackage();
        collectEndpointClasses(endpointClasses, endpointExposedClasses,
                nonEndpointClasses, testClasses);
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

    protected void generateOpenApi(URL customApplicationProperties) {
        Properties applicationProperties = customApplicationProperties == null
                ? new Properties()
                : TestUtils
                        .readProperties(customApplicationProperties.getPath());
        new OpenAPISpecGenerator(applicationProperties).generateOpenApiSpec(
                TestUtils.getClassFilePath(testPackage), openApiJsonOutput);
    }

    protected void generateTsEndpoints() {
        new MainGenerator(openApiJsonOutput.toFile(), outputDirectory.getRoot())
                .start();
    }

    protected OpenAPI getOpenApiObject() {
        OpenAPIObjectGenerator generator = new OpenAPIObjectGenerator();

        Path javaSourcePath = Paths.get("src/test/java/",
                testPackage.getName().replace('.', File.separatorChar));
        generator.addSourcePath(javaSourcePath);

        generator.setOpenApiConfiguration(new OpenAPIConfiguration("Test title",
                "0.0.1", "https://server.test", "Test description"));

        return generator.getOpenApi();
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

    @Before
    public void setUpOutputFile() {
        openApiJsonOutput = Paths.get(
                outputDirectory.getRoot().getAbsolutePath(), "openapi.json");
    }

}
