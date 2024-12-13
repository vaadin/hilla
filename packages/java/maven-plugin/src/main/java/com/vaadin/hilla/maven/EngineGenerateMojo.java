package com.vaadin.hilla.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.plugin.maven.FlowModeAbstractMojo;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserException;
import com.vaadin.hilla.engine.ParserProcessor;

import static com.vaadin.flow.server.frontend.FrontendUtils.FRONTEND;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EngineGenerateMojo extends AbstractMojo
        implements Configurable {
    /**
     * A directory with project's frontend source files.
     */
    @Parameter(property = "frontendDirectory", defaultValue = "${project.basedir}/src/main/"
            + FRONTEND)
    private File frontend;

    /**
     * The folder where TypeScript endpoints are generated.
     */
    @Parameter(property = "generatedTsFolder")
    private File generated;

    @Parameter(property = "nodeCommand", defaultValue = "node")
    private String node;

    @Parameter(property = "mainClass")
    private String mainClass;

    @Override
    public void execute() throws EngineGenerateMojoException {
        var project = (MavenProject) getPluginContext().get("project");
        if (!FlowModeAbstractMojo.isHillaAvailable(project)) {
            getLog().warn(
                    """
                            The 'generate' goal is only meant to be used in Hilla projects with endpoints.
                            """
                            .stripIndent());
            return;
        }
        try {
            var conf = configure();
            var parserProcessor = new ParserProcessor(conf);
            var generatorProcessor = new GeneratorProcessor(conf);

            var endpoints = conf.getEndpointFinder().findEndpoints();
            parserProcessor.process(endpoints);
            generatorProcessor.process();
        } catch (ExecutionFailedException e) {
            throw new EngineGenerateMojoException("Endpoint collection failed",
                    e);
        } catch (GeneratorException | ParserException e) {
            throw new EngineGenerateMojoException("Execution failed", e);
        }
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String getMainClass() {
        return mainClass;
    }

    @Override
    public File getFrontend() {
        return frontend;
    }

    @Override
    public File getGenerated() {
        return generated;
    }
}
