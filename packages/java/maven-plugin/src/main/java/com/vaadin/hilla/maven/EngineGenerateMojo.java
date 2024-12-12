package com.vaadin.hilla.maven;

import com.vaadin.flow.server.ExecutionFailedException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.plugin.maven.FlowModeAbstractMojo;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointExposed;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserException;
import com.vaadin.hilla.engine.ParserProcessor;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(goal = "configure")
public final class EngineGenerateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws EngineGenerateMojoException {
        if (!FlowModeAbstractMojo.isHillaAvailable(project)) {
            getLog().warn(
                    """
                            The 'generate' goal is only meant to be used in Hilla projects with endpoints.
                            """
                            .stripIndent());
            return;
        }
        try {
            var conf = EngineConfiguration.getDefault();
            var parserProcessor = new ParserProcessor(conf);
            var generatorProcessor = new GeneratorProcessor(conf);

            var endpoints = conf.getOfflineEndpointProvider().findEndpoints();
            parserProcessor.process(endpoints);
            generatorProcessor.process();
        } catch (ExecutionFailedException e) {
            throw new EngineGenerateMojoException("Endpoint collection failed",
                    e);
        } catch (GeneratorException | ParserException e) {
            throw new EngineGenerateMojoException("Execution failed", e);
        }
    }
}
