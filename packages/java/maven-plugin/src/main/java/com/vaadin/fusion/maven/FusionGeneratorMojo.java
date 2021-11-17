package com.vaadin.fusion.maven;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;

/**
 * Fusion plugin for Maven. Handles loading the parser and its plugins.
 */
@Mojo(name = "fusion-generator", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class FusionGeneratorMojo extends AbstractMojo {
    @Parameter(readonly = true)
    private String configPath;

    @Parameter(readonly = true)
    private String openAPITemplatePath;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() {
        var classPath = ((List<String>) project.getCompileClasspathElements())
                .stream().collect(Collectors.joining(";"));

        var configBuilder = new ParserConfig.Builder();

        if (configPath != null) {
            configBuilder.configFile(Paths.get(configPath).toFile());
        }

        if (openAPITemplatePath != null) {
            configBuilder
                    .openAPITemplate(Paths.get(openAPITemplatePath).toFile());
        }

        var config = configBuilder.classPath(classPath, false)
                .adjustOpenAPI(openAPI -> {
                    var info = openAPI.getInfo();

                    if (openAPITemplatePath == null
                            || info.getTitle() == null) {
                        info.setTitle(project.getName());
                    }

                    if (openAPITemplatePath == null
                            || info.getVersion() == null) {
                        info.setVersion(project.getVersion());
                    }
                }).finish();

        new Parser(config).execute();
    }
}
