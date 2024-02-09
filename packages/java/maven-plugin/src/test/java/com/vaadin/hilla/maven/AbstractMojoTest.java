package com.vaadin.hilla.maven;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.parser.testutils.TestEngineConfigurationPathResolver;

/**
 * Base class for Engine Maven plugin tests. Delegates to
 * {@link AbstractMojoTestCase} from
 * {@code "org.apache.maven.plugin-testing:maven-plugin-testing-harness"}.
 */
public class AbstractMojoTest {
    private final DelegateMojoTestCase testCase = new DelegateMojoTestCase();
    private Path buildDirectory;
    private EngineConfiguration.Builder configurationBuilder;
    private Path outputDirectory;
    private MavenProject project;
    private Path temporaryDirectory;

    @BeforeEach
    public void setUpMojoTest() throws Exception {
        testCase.setUp();

        temporaryDirectory = Files.createTempDirectory(getClass().getName());
        buildDirectory = temporaryDirectory.resolve("build");
        outputDirectory = buildDirectory.resolve("classes");
        Files.createDirectories(outputDirectory);
        // Add fake com.vaadin.hilla.EndpointController class to make project
        // detected as Hilla project with endpoints.
        Files.createDirectories(
                buildDirectory.resolve("test-classes/com/vaadin/hilla"));
        Files.createFile(buildDirectory.resolve(
                "test-classes/com/vaadin/hilla/EndpointController.class"));
        // Maven project is not initialized on the mojo, setup a mock manually
        project = Mockito.mock(MavenProject.class);
        // Using Path.of here to have correct separators at Windows & Unix
        var classPathElements = List
                .of(buildDirectory.resolve("test-classes").toString());
        Mockito.doReturn(classPathElements).when(project)
                .getCompileClasspathElements();
        Mockito.doReturn(classPathElements).when(project)
                .getRuntimeClasspathElements();
        Mockito.doReturn(getTemporaryDirectory().toFile()).when(project)
                .getBasedir();
        var mockBuild = Mockito.mock(Build.class);
        Mockito.doReturn("build").when(mockBuild).getDirectory();
        Mockito.doReturn(Path.of("build/classes").toString()).when(mockBuild)
                .getOutputDirectory();
        Mockito.doReturn(mockBuild).when(project).getBuild();

        var configFilePath = getBuildDirectory()
                .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME);

        // Load reference EngineConfiguration
        Files.copy(
                Path.of(Objects
                        .requireNonNull(getClass().getResource(
                                EngineConfiguration.DEFAULT_CONFIG_FILE_NAME))
                        .toURI()),
                configFilePath);

        var config = TestEngineConfigurationPathResolver.resolve(
                EngineConfiguration.load(configFilePath.toFile()),
                temporaryDirectory);

        assertNotNull(config, "expected reference "
                + "EngineConfiguration to load from json");
        configurationBuilder = new EngineConfiguration.Builder(config)
                .baseDir(getTemporaryDirectory());

        // Delete reference json file from temporary directory
        Files.delete(getBuildDirectory()
                .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME));
    }

    @AfterEach
    public void tearDownMojoTest() throws Exception {
        testCase.tearDown();

        try (var paths = Files.walk(temporaryDirectory)) {
            var pathList = paths.sorted(Comparator.reverseOrder()).toList();
            for (var path : pathList) {
                Files.delete(path);
            }
        }
    }

    protected Path getBuildDirectory() {
        return buildDirectory;
    }

    protected EngineConfiguration getEngineConfiguration() {
        return configurationBuilder.create();
    }

    protected MavenProject getMavenProject() {
        return project;
    }

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    protected File getTestConfiguration() throws URISyntaxException {
        return new File(Objects
                .requireNonNull(getClass()
                        .getResource(getClass().getSimpleName() + ".xml"))
                .toURI());
    }

    protected Mojo lookupMojo(String name, File pom) throws Exception {
        return testCase.lookupMojo(name, pom);
    }

    protected void setVariableValueToObject(Object object, String variable,
            Object value) throws IllegalAccessException {
        testCase.setVariableValueToObject(object, variable, value);
    }

    public static class DelegateMojoTestCase extends AbstractMojoTestCase {
        @Override
        protected Mojo lookupMojo(String goal, File pom) throws Exception {
            return super.lookupMojo(goal, pom);
        }

        protected void setUp() throws Exception {
            super.setUp();
        }

        @Override
        protected void setVariableValueToObject(Object object, String variable,
                Object value) throws IllegalAccessException {
            super.setVariableValueToObject(object, variable, value);
        }

        protected void tearDown() throws Exception {
            super.tearDown();
        }
    }
}
