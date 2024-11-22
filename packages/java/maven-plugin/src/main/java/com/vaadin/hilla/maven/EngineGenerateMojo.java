package com.vaadin.hilla.maven;

import java.io.File;

import com.vaadin.flow.server.ExecutionFailedException;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.plugin.maven.FlowModeAbstractMojo;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserException;
import com.vaadin.hilla.engine.ParserProcessor;

import static com.vaadin.flow.server.frontend.FrontendUtils.FRONTEND;
import static com.vaadin.flow.server.frontend.FrontendUtils.GENERATED;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EngineGenerateMojo extends AbstractMojo {
    /**
     * A directory with project's frontend source files.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/" + FRONTEND)
    private File frontendDirectory;

    @Parameter(defaultValue = "${null}")
    private File generatedTsFolder;

    @Parameter(defaultValue = "node")
    private String nodeCommand;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "spring-boot.aot.main-class")
    private String mainClass;

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
            var isProduction = project.getActiveProfiles().stream()
                    .map(Profile::getId).anyMatch("production"::equals);
            var conf = new EngineConfiguration.Builder()
                    .baseDir(project.getBasedir().toPath())
                    .buildDir(project.getBuild().getDirectory())
                    .outputDir(generatedTsFolder().toPath())
                    .groupId(project.getGroupId())
                    .artifactId(project.getArtifactId()).mainClass(mainClass)
                    .productionMode(isProduction).create();
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

    private File generatedTsFolder() {
        if (generatedTsFolder != null) {
            return generatedTsFolder;
        }
        return new File(frontendDirectory, GENERATED);
    }
}
