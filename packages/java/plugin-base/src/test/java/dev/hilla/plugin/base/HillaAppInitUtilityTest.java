package dev.hilla.plugin.base;

import com.vaadin.flow.server.frontend.FrontendUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_FRONTEND_DIR;

public class HillaAppInitUtilityTest {

    private Path projectDirectory;

    @BeforeEach
    public void setup() throws IOException {
        projectDirectory = Files.createTempDirectory(getClass().getName());
        projectDirectory.toFile().deleteOnExit();
        Files.createDirectories(projectDirectory.resolve("src/main/java"));
        var userDir = projectDirectory.toAbsolutePath().toString();
        System.setProperty("user.dir", userDir);
        System.clearProperty(PARAM_FRONTEND_DIR);
    }

    @AfterEach
    public void tearDownTaskApplication() throws IOException {
        FrontendUtils.deleteDirectory(projectDirectory.toFile());
    }

    @Test
    public void noHillaDependencyDefined_expectIllegalArgumentException() {

        RuntimeException thrown = Assertions
                .assertThrows(RuntimeException.class, () -> HillaAppInitUtility
                        .scaffold(projectDirectory, List.of()));

        Assertions.assertTrue(
                thrown.getMessage().startsWith("No hilla starter found"));
    }

    @Test
    public void noSpringBootApplicationClassExistInProject_expectRuntimeException() {

        var dependencyArtifactIds = List.of("hilla",
                "hilla-spring-boot-starter", "spring-boot-starter-validation",
                "spring-boot-devtools", "spring-boot-starter-test",
                "vaadin-testbench-junit5");

        RuntimeException thrown = Assertions
                .assertThrows(RuntimeException.class, () -> HillaAppInitUtility
                        .scaffold(projectDirectory, dependencyArtifactIds));

        Assertions.assertEquals(
                "No class annotated with @SpringBootApplication found!",
                thrown.getMessage());
    }

    @Test
    public void withCorrectHillaReactDependencies_scaffoldForReactSucceeds()
            throws IOException {

        createSpringBootApplicationClass();

        var dependencyArtifactIds = List.of("hilla-react",
                "hilla-spring-boot-starter", "spring-boot-starter-validation",
                "spring-boot-devtools", "spring-boot-starter-test",
                "vaadin-testbench-junit5");

        HillaAppInitUtility.scaffold(projectDirectory, dependencyArtifactIds);

        Assertions.assertTrue(projectDirectory
                .resolve("src/main/java/my/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory
                .resolve("src/main/java/my/endpoints/package-info.java")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("package.json").toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("package-lock.json")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("frontend/index.ts")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("types.d.ts").toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("vite.config.ts").toFile().exists());

        Assertions.assertTrue(
                projectDirectory.resolve("frontend/App.tsx").toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("frontend/routes.tsx")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory
                .resolve("frontend/views/MainView.tsx").toFile().exists());

        assertEndpointIsNotGeneratedForInvalidPackages();
    }

    @Test
    public void withCorrectHillaReactSpringBootStarterDependencies_scaffoldForReactSucceeds()
            throws IOException {

        createSpringBootApplicationClass();

        var dependencyArtifactIds = List.of("hilla-react-spring-boot-starter",
                "spring-boot-starter-validation", "spring-boot-devtools",
                "spring-boot-starter-test", "vaadin-testbench-junit5");

        HillaAppInitUtility.scaffold(projectDirectory, dependencyArtifactIds);

        Assertions.assertTrue(projectDirectory
                .resolve("src/main/java/my/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory
                .resolve("src/main/java/my/endpoints/package-info.java")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("package.json").toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("package-lock.json")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("frontend/index.ts")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("types.d.ts").toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("vite.config.ts").toFile().exists());

        Assertions.assertTrue(
                projectDirectory.resolve("frontend/App.tsx").toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("frontend/routes.tsx")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory
                .resolve("frontend/views/MainView.tsx").toFile().exists());

        assertEndpointIsNotGeneratedForInvalidPackages();
    }

    @Test
    public void withCorrectLitDependencies_scaffoldForLitSucceeds()
            throws IOException {

        createSpringBootApplicationClass();

        var dependencyArtifactIds = List.of("hilla",
                "hilla-spring-boot-starter", "spring-boot-starter-validation",
                "spring-boot-devtools", "spring-boot-starter-test",
                "vaadin-testbench-junit5");

        HillaAppInitUtility.scaffold(projectDirectory, dependencyArtifactIds);

        Assertions.assertTrue(projectDirectory
                .resolve("src/main/java/my/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("package.json").toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("package-lock.json")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory.resolve("frontend/index.ts")
                .toFile().exists());
        Assertions.assertTrue(
                projectDirectory.resolve("vite.config.ts").toFile().exists());

        Assertions.assertTrue(projectDirectory.resolve("frontend/routes.ts")
                .toFile().exists());
        Assertions.assertTrue(projectDirectory
                .resolve("frontend/views/main-view.ts").toFile().exists());

        assertEndpointIsNotGeneratedForInvalidPackages();
    }

    private void assertEndpointIsNotGeneratedForInvalidPackages() {
        Assertions.assertFalse(projectDirectory.resolve(
                "src/main/java/my/package1/app/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertFalse(projectDirectory.resolve(
                "src/main/java/my/single/line/comment/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertFalse(projectDirectory.resolve(
                "src/main/java/my/javadoc/package1/endpoints/HelloEndpoint.java")
                .toFile().exists());
        Assertions.assertFalse(projectDirectory.resolve(
                "src/main/java/my/package1/in/string/literal/endpoints/HelloEndpoint.java")
                .toFile().exists());
    }

    private void createSpringBootApplicationClass() throws IOException {

        var content = """
                /*
                * Some comments that might also contains a package
                * declaration statement:
                * package my.package1.app;
                */

                /*
                 Some multi-line comments might not have * at the begiining
                 of each line, especially before package declaration statement:
                 package my.package1.app;
                */

                // single line comments might also contain package declarations:
                // package my.single.line.comment;

                /**
                 * javadocs may contain package declarations:
                 * package my.javadoc.package1;
                 */

                package   my ;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class Application {
                    String myPackage = "my.package1.in.string.literal";
                    public static void main(String[] args) {
                        SpringApplication.run(Application.class, args);
                    }
                }
                """
                .stripIndent();

        var springBootAppClass = Files.createFile(projectDirectory
                .resolve("src/main/java").resolve("Application.java"));
        Files.writeString(springBootAppClass, content);
    }
}
