package dev.hilla.maven;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "init-react-app", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class InitReactAppMojo extends AbstractMojo {
    public static final String SKELETON_URL = "https://github.com/vaadin/skeleton-starter-hilla-react/archive/refs/heads/v2.zip";

    @Override
    public void execute() throws MojoFailureException {

        try {
            var zipFile = Files.createTempFile("scaffold", ".zip");
            zipFile.toFile().deleteOnExit();
            Files.copy(new URL(SKELETON_URL).openStream(), zipFile,
                    StandardCopyOption.REPLACE_EXISTING);

            var items = List.of("package.json", "package-lock.json",
                    "frontend/App.tsx", "frontend/index.ts",
                    "frontend/routes.tsx", "frontend/views/MainView.tsx",
                    "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");

            try (var zip = new ZipFile(zipFile.toFile())) {
                var entries = zip.entries();

                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();

                    if (entry.getName().contains("/")) {
                        var item = entry.getName().split("/", 2)[1];

                        if (items.contains(item)) {
                            var path = Path.of(item);
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
        } catch (IOException e) {
            throw new MojoFailureException(e);
        }
    }
}
