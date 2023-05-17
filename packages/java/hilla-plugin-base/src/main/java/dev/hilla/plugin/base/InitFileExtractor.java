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

public class InitFileExtractor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(InitFileExtractor.class);
    private static final String REACT_SKELETON = "https://github.com/vaadin/skeleton-starter-hilla-react/archive/refs/heads/v2.1.zip";
    private static final List<String> REACT_FILE_LIST = List.of("package.json",
            "package-lock.json", "frontend/App.tsx", "frontend/index.ts",
            "frontend/routes.tsx", "frontend/views/MainView.tsx",
            "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");
    private static final String LIT_SKELETON = "https://github.com/vaadin/skeleton-starter-hilla-lit/archive/refs/heads/v2.1.zip";
    private static final List<String> LIT_FILE_LIST = List.of("package.json",
            "package-lock.json", "frontend/App.ts", "frontend/index.ts",
            "frontend/routes.ts", "frontend/views/MainView.tsx",
            "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");

    private final Path projectDirectory;

    public InitFileExtractor(Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void execute() throws IOException {
        var framework = detectFramework();
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
                        if (item.endsWith("Endpoint.java")) {
                            var applicationPackage = findSpringBootApplicationPackage();
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

                            LOGGER.info("Adding endpoint {}", item);
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

    private enum Frameworks {
        REACT(REACT_SKELETON, REACT_FILE_LIST), LIT(LIT_SKELETON,
                LIT_FILE_LIST);

        private final String skeletonUrl;
        private final List<String> items;

        Frameworks(String skeletonUrl, List<String> items) {
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

    private Frameworks detectFramework() throws IOException {
        var buildFiles = List.of("pom.xml", "build.gradle", "build.gradle.kts");
        var buildFile = buildFiles.stream()
                .filter(file -> Files.exists(projectDirectory.resolve(file)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "No build file found"));

        // search `hilla-react-spring-boot-starter` or
        // `hilla-spring-boot-starter` in build file
        var content = Files.readString(projectDirectory.resolve(buildFile));
        if (content.contains("hilla-react-spring-boot-starter")) {
            return Frameworks.REACT;
        } else if (content.contains("hilla-spring-boot-starter")) {
            return Frameworks.LIT;
        } else {
            throw new IllegalArgumentException("No hilla starter found");
        }
    }

    private String findSpringBootApplicationPackage() throws IOException {
        return Files.walk(projectDirectory)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> {
                    try {
                        var content = Files.readString(path);
                        return content.contains("@SpringBootApplication");
                    } catch (IOException e) {
                        LOGGER.error("Error reading file: {}", path, e);
                        return false;
                    }
                }).map(this::getPackageName).findFirst()
                .orElseThrow(() -> new IOException(
                        "Spring Boot application class not found"));
    }

    private String getPackageName(Path path) {
        try {
            var content = Files.readString(path);
            var matcher = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;")
                    .matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Error reading file: {}", path, e);
            return null;
        }
    }
}
