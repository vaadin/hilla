package com.vaadin.fusion.maven;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.vaadin.fusion.maven.generator.GeneratorConfiguration;
import com.vaadin.fusion.maven.generator.GeneratorException;
import com.vaadin.fusion.maven.generator.GeneratorProcessor;
import com.vaadin.fusion.maven.parser.ParserConfiguration;
import com.vaadin.fusion.maven.parser.ParserException;
import com.vaadin.fusion.maven.parser.ParserProcessor;

/**
 * Fusion plugin for Maven. Handles loading the parser and its
 * pluginSpecifications.
 */
@Mojo(name = "generator", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class FusionGeneratorMojo extends AbstractMojo {
    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();
    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() throws FusionGeneratorMojoException {
        var openAPI = parseJavaCode();
        generateTypeScriptCode(openAPI);
    }

    private void generateTypeScriptCode(String openAPI)
            throws FusionGeneratorMojoException {
        var logger = getLog();
        try {
            var executor = new GeneratorProcessor(project, logger);

            executor.useInput(openAPI);
            generator.getOutputDir().ifPresentOrElse(executor::useOutputDir,
                    executor::useOutputDir);
            generator.getPlugins().ifPresentOrElse(executor::usePlugins,
                    executor::usePlugins);
            executor.useVerbose(logger.isDebugEnabled());

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new FusionGeneratorMojoException("TS code generation failed",
                    e);
        }
    }

    private String parseJavaCode() throws FusionGeneratorMojoException {
        try {
            var executor = new ParserProcessor(project, getLog());

            parser.getClassPath().ifPresentOrElse(executor::useClassPath,
                    executor::useClassPath);
            parser.getEndpointAnnotation().ifPresentOrElse(
                    executor::useEndpointAnnotation,
                    executor::useEndpointAnnotation);
            parser.getOpenAPIPath().ifPresent(executor::useOpenAPIBase);
            parser.getPlugins().ifPresentOrElse(executor::usePlugins,
                    executor::usePlugins);

            return executor.process();
        } catch (ParserException e) {
            throw new FusionGeneratorMojoException("Java code parsing failed",
                    e);
        }
    }
}
