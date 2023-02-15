package dev.hilla.maven;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import dev.hilla.engine.EngineConfiguration;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for Engine Maven plugin tests. Delegates to
 * {@link AbstractMojoTestCase} from
 * {@code "org.apache.maven.plugin-testing:maven-plugin-testing-harness"}.
 */
public class AbstractMojoTest {
    private Path temporaryDirectory;
    private Path buildDirectory;
    private EngineConfiguration engineConfiguration;

    private final DelegateMojoTestCase testCase = new DelegateMojoTestCase();

    private MavenProject project;

    @BeforeEach
    public void setUpMojoTest() throws Exception {
        testCase.setUp();

        temporaryDirectory = Files.createTempDirectory(getClass().getName());
        buildDirectory = temporaryDirectory.resolve("build");
        Files.createDirectories(buildDirectory);

        // Maven project is not initialized on the mojo, setup a mock manually
        project = Mockito.mock(MavenProject.class);
        var classPathElements = List.of("target/test-classes");
        Mockito.doReturn(classPathElements).when(project)
                .getCompileClasspathElements();
        Mockito.doReturn(classPathElements).when(project)
                .getRuntimeClasspathElements();
        Mockito.doReturn(getTemporaryDirectory().toFile()).when(project)
                .getBasedir();
        var mockBuild = Mockito.mock(Build.class);
        Mockito.doReturn("build").when(mockBuild).getDirectory();
        Mockito.doReturn(mockBuild).when(project).getBuild();

        // Load reference EngineConfiguration
        Files.copy(
                Path.of(Objects
                        .requireNonNull(getClass()
                                .getResource(EngineConfiguration.RESOURCE_NAME))
                        .toURI()),
                getBuildDirectory().resolve(EngineConfiguration.RESOURCE_NAME));

        engineConfiguration = EngineConfiguration
                .load(getBuildDirectory().toFile());
        assertNotNull(engineConfiguration, "expected reference "
                + "EngineConfiguration to load from json");
        engineConfiguration.setBaseDir(getTemporaryDirectory());

        // Delete reference json file from temporary directory
        Files.delete(
                getBuildDirectory().resolve(EngineConfiguration.RESOURCE_NAME));
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

    protected Mojo lookupMojo(String name, File pom) throws Exception {
        return testCase.lookupMojo(name, pom);
    }

    protected File getTestConfigurartion() throws URISyntaxException {
        return new File(Objects
                .requireNonNull(getClass()
                        .getResource(getClass().getSimpleName() + ".xml"))
                .toURI());
    }

    protected void setVariableValueToObject(Object object, String variable,
            Object value) throws IllegalAccessException {
        testCase.setVariableValueToObject(object, variable, value);
    }

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    protected Path getBuildDirectory() {
        return buildDirectory;
    }

    protected EngineConfiguration getEngineConfiguration() {
        return engineConfiguration;
    }

    protected MavenProject getMavenProject() {
        return project;
    }

    public static class DelegateMojoTestCase extends AbstractMojoTestCase {
        protected void setUp() throws Exception {
            super.setUp();
        }

        protected void tearDown() throws Exception {
            super.tearDown();
        }

        @Override
        protected Mojo lookupMojo(String goal, File pom) throws Exception {
            return super.lookupMojo(goal, pom);
        }

        @Override
        protected void setVariableValueToObject(Object object, String variable,
                Object value) throws IllegalAccessException {
            super.setVariableValueToObject(object, variable, value);
        }
    }
}
