package dev.hilla.maven;

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

    private final String skeletonUrl;
    private final List<String> items;
    private final Path projectDirectory;

    public InitFileExtractor(String skeletonUrl, List<String> items,
            Path projectDirectory) {
        this.skeletonUrl = skeletonUrl;
        this.items = items;
        this.projectDirectory = projectDirectory;
    }

    public void execute() throws IOException {
        var zipFile = Files.createTempFile("hilla-scaffold", ".zip");
        zipFile.toFile().deleteOnExit();

        try (var is = new URL(skeletonUrl).openStream()) {
            Files.copy(is, zipFile, StandardCopyOption.REPLACE_EXISTING);
        }

        try (var zip = new ZipFile(zipFile.toFile())) {
            var entries = zip.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();

                if (entry.getName().contains("/")) {
                    var item = entry.getName().split("/", 2)[1];

                    if (items.contains(item)) {
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
