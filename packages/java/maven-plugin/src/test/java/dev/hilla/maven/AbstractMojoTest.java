package dev.hilla.maven;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Engine Maven plugin tests. Delegates to
 * {@link AbstractMojoTestCase} from {@code "org.apache.maven.plugin-testing:maven-plugin-testing-harness"}.
 */
public class AbstractMojoTest {
    private Path temporaryDirectory;

    private final DelegateMojoTestCase testCase = new DelegateMojoTestCase();

    @BeforeEach
    public void setUpMojoTest() throws Exception {
        testCase.setUp();

        temporaryDirectory = Files.createTempDirectory(getClass().getName());
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

    public Mojo lookupMojo(String name, File pom) throws Exception {
        return testCase.lookupMojo(name, pom);
    }

    public File getTestConfigurartion() throws URISyntaxException {
        return new File(
            Objects.requireNonNull(
                    getClass().getResource(getClass().getSimpleName() + ".xml"))
                .toURI());
    }

    protected Path getTemporaryDirectory() {
        return temporaryDirectory;
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
    }
}
