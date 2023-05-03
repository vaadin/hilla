package dev.hilla.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "init-lit-app", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class InitLitAppMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}")
    private Path projectBaseDir;

    @Override
    public void execute() throws MojoFailureException {
        var skeletonUrl = "https://github.com/vaadin/skeleton-starter-hilla-lit/archive/refs/heads/v2.1.zip";
        var items = List.of("package.json", "package-lock.json",
                "frontend/App.ts", "frontend/index.ts", "frontend/routes.ts",
                "frontend/views/MainView.tsx",
                "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");
        var extractor = new InitFileExtractor(skeletonUrl, items,
                projectBaseDir);

        try {
            extractor.execute();
        } catch (IOException e) {
            throw new MojoFailureException(e);
        }
    }
}
