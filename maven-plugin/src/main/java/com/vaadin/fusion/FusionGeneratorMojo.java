package com.vaadin.fusion;

import java.io.File;
import java.nio.file.Paths;
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
    @Parameter(required = true, readonly = true)
    private String configPath;
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() {
        String classPath = (String) project.getCompileClasspathElements()
                .stream().collect(Collectors.joining(";"));

        File configFile = Paths.get(configPath).toFile();

        ParserConfig config = new ParserConfig.Factory(configFile)
                .classPath(classPath, false)
                .applicationName(project.getName(), false)
                .applicationVersion(project.getVersion(), false).finish();

        new Parser(config).execute();
    }
}
