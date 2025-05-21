package com.vaadin.hilla.maven;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.hilla.engine.EngineAutoConfiguration;

/**
 * Base class for Engine Maven plugin tests. Delegates to
 * {@link AbstractMojoTestCase} from
 * {@code "org.apache.maven.plugin-testing:maven-plugin-testing-harness"}.
 */
public class AbstractMojoTest {
    private final DelegateMojoTestCase testCase = new DelegateMojoTestCase();
    private Path buildDirectory;
    private EngineAutoConfiguration engineConfiguration;
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
        project = createMavenProject();

        engineConfiguration = new EngineAutoConfiguration.Builder()
                .baseDir(temporaryDirectory)
                .browserCallableFinder((conf) -> List.of()).build();
        EngineAutoConfiguration.setDefault(engineConfiguration);
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

    protected EngineAutoConfiguration getEngineConfiguration() {
        return engineConfiguration;
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

    MavenProject createMavenProject() {
        MavenProject project = new MavenProject();
        project.setGroupId("com.vaadin.testing");
        project.setArtifactId("my-application");
        project.setFile(temporaryDirectory.resolve("pom.xml").toFile());
        project.setBuild(new Build());
        project.getBuild().setFinalName("finalName");
        project.getBuild().setDirectory("build");
        project.getBuild().setOutputDirectory("build/classes");

        DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler();
        artifactHandler.setAddedToClasspath(true);
        DefaultArtifact artifact = new DefaultArtifact(
                "com.vaadin.hilla.testing", "mock-hilla", "1.0", "compile",
                "jar", null, artifactHandler);
        artifact.setFile(buildDirectory.resolve("test-classes").toFile());
        project.setArtifacts(Set.of(artifact));
        return project;
    }

}
