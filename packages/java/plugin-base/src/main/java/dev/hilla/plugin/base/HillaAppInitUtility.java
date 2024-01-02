package dev.hilla.plugin.base;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HillaAppInitUtility {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HillaAppInitUtility.class);

    private static final String REACT_SKELETON = "https://github.com/vaadin/skeleton-starter-hilla-react/archive/refs/heads/v2.zip";

    private static final List<String> REACT_FILE_LIST = List.of("package.json",
            "package-lock.json", "types.d.ts", "vite.config.ts",
            "frontend/App.tsx", "frontend/index.ts", "frontend/routes.tsx",
            "frontend/views/MainView.tsx",
            "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java",
            "src/main/java/org/vaadin/example/endpoints/package-info.java");

    private static final String LIT_SKELETON = "https://github.com/vaadin/skeleton-starter-hilla-lit/archive/refs/heads/v2.zip";

    private static final List<String> LIT_FILE_LIST = List.of("package.json",
            "package-lock.json", "vite.config.ts", "frontend/index.ts",
            "frontend/routes.ts", "frontend/views/main-view.ts",
            "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");

    private enum Framework {

        REACT(REACT_SKELETON, REACT_FILE_LIST), LIT(LIT_SKELETON,
                LIT_FILE_LIST);

        private final String skeletonUrl;
        private final List<String> items;

        Framework(String skeletonUrl, List<String> items) {
            this.skeletonUrl = skeletonUrl;
            this.items = items;
        }

        public String getSkeletonUrl() {
            return skeletonUrl;
        }

        public List<String> getItems() {
            return items;
        }
    }

    public static void scaffold(Path projectDirectory,
            List<String> dependencyArtifactIds) throws IOException {
        var framework = detectFramework(dependencyArtifactIds);
        var zipFile = Files.createTempFile("hilla-scaffold", ".zip");
        zipFile.toFile().deleteOnExit();

        try (var is = new URL(framework.getSkeletonUrl()).openStream()) {
            Files.copy(is, zipFile, StandardCopyOption.REPLACE_EXISTING);
        }

        try (var zip = new ZipFile(zipFile.toFile())) {
            var entries = zip.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();

                if (entry.getName().contains("/")) {
                    var item = entry.getName().split("/", 2)[1];

                    if (framework.getItems().contains(item)) {
                        if (item.endsWith("Endpoint.java")
                                || item.endsWith("package-info.java")) {
                            var applicationPackage = findSpringBootApplicationPackage(
                                    projectDirectory);
                            var applicationPath = applicationPackage
                                    .replace(".", "/");
                            // read file to string
                            var content = new String(
                                    zip.getInputStream(entry).readAllBytes());
                            // replace package with application package
                            content = content.replace("org.vaadin.example",
                                    applicationPackage);
                            // adjust path
                            item = item.replace("org/vaadin/example",
                                    applicationPath);
                            // write file
                            var path = projectDirectory.resolve(item);
                            var parent = path.getParent();

                            if (parent != null) {
                                Files.createDirectories(parent);
                            }

                            LOGGER.info("Adding endpoint source {}", item);
                            Files.writeString(path, content);
                        } else {
                            LOGGER.info("Extracting {}", item);
                            var path = projectDirectory.resolve(item);
                            var parent = path.getParent();

                            if (parent != null) {
                                Files.createDirectories(parent);
                            }

                            Files.copy(zip.getInputStream(entry), path,
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
    }

    private static Framework detectFramework(
            List<String> dependencyArtifactIds) {

        if (dependencyArtifactIds.stream()
                .anyMatch(artifactId -> artifactId.contains("hilla-react"))) {
            return Framework.REACT;
        }

        if (dependencyArtifactIds.stream()
                .anyMatch(artifactId -> artifactId.contains("hilla"))) {
            return Framework.LIT;
        }

        throw new RuntimeException("No hilla starter found! "
                + "To use hilla:init-app maven goal (or hillaInitApp task in gradle), you must either have "
                + "'hilla-react-spring-boot-starter' or 'hilla-spring-boot-starter' "
                + "in the list of your dependencies. %nPlease take a look at "
                + "https://github.com/vaadin/skeleton-starter-hilla-react or "
                + "https://github.com/vaadin/skeleton-starter-hilla-lit as a reference.");
    }

    private static String findSpringBootApplicationPackage(
            Path projectDirectory) throws IOException {
        try (var paths = Files.walk(projectDirectory.resolve("src/main"))) {
            return paths
                    .filter(path -> !path.toString().contains("/resources/"))
                    .filter(path -> path.toFile().isFile()
                            && !path.toFile().getName().startsWith("."))
                    .filter(path -> {
                        try {
                            var content = Files.readString(path);
                            return content.contains("@SpringBootApplication");
                        } catch (IOException e) {
                            LOGGER.error("Error reading file: {}", path, e);
                            return false;
                        }
                    }).map(HillaAppInitUtility::extractPackage).findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No class annotated with @SpringBootApplication found!"));
        }
    }

    private static String extractPackage(Path path) {
        try {
            var content = Files.readString(path);
            var regexToExcludeCommentsAndStringLiterals = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
            var codeWithoutComments = content
                    .replaceAll(regexToExcludeCommentsAndStringLiterals, "");

            var regexToExtractPackage = "package\\b\\s+([a-zA-Z_][\\w.]*)\\s*;";
            var matcher = Pattern.compile(regexToExtractPackage)
                    .matcher(codeWithoutComments);
            if (matcher.find()) {
                return matcher.group(1);
            }
            // no package declaration means the class is at the default package:
            throw new RuntimeException(
                    "Having the class annotated with @SpringBootApplication at "
                            + "the default package is not allowed by the Spring Boot as "
                            + "the component scan will fail during startup.");
        } catch (IOException e) {
            var errorMessage = String.format("Error reading file: %s", path);
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
