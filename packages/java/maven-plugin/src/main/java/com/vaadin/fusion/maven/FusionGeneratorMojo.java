package com.vaadin.fusion.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Fusion plugin for Maven. Handles loading the parser and its
 * pluginSpecifications.
 */
@Mojo(name = "fusion-generator", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class FusionGeneratorMojo extends AbstractMojo {
    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();

    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() {
        var openAPI = parseJavaCode();
        generateTypeScriptCode(openAPI);
    }

    private String parseJavaCode() {
        var executor = new ParserExecutor(project);

        parser.getClassPath().ifPresentOrElse(executor::useClassPath,
            executor::useClassPath);
        parser.getEndpointAnnotation().ifPresent(executor::useEndpointAnnotation);
        parser.getOpenAPIPath().ifPresent(executor::useOpenAPIBase);
        parser.getPlugins().ifPresentOrElse(executor::usePlugins, executor::usePlugins);

        return executor.execute();
    }

    private void generateTypeScriptCode(String openAPI) {
        var executor = new GeneratorExecutor(project);

        executor.useInput(openAPI);
        generator.getOutputDir().ifPresentOrElse(executor::useOutputDir, executor::useOutputDir);
        generator.getPlugins().ifPresentOrElse(executor::usePlugins, executor::usePlugins);

        executor.execute();
    }
}
