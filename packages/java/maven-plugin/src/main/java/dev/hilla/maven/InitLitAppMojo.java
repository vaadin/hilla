package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import dev.hilla.plugin.base.InitFileExtractor;

@Mojo(name = "init-lit-app", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class InitLitAppMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}")
    private File projectBaseDir;

    @Override
    public void execute() throws MojoFailureException {
        var skeletonUrl = "https://github.com/vaadin/skeleton-starter-hilla-lit/archive/refs/heads/v2.1.zip";
        var items = List.of("package.json", "package-lock.json",
                "frontend/App.ts", "frontend/index.ts", "frontend/routes.ts",
                "frontend/views/MainView.tsx",
                "src/main/java/org/vaadin/example/endpoints/HelloEndpoint.java");
        var extractor = new InitFileExtractor(skeletonUrl, items,
                projectBaseDir.toPath());

        try {
            extractor.execute();
        } catch (IOException e) {
            throw new MojoFailureException(e);
        }
    }
}
