package dev.hilla.maven;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.RUNTIME)
public final class EndpointCodeGeneratorMojo extends AbstractMojo {
    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();
    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() throws EndpointCodeGeneratorMojoException {
        var result = parseJavaCode();
        generateTypeScriptCode(result);
    }

    private void generateTypeScriptCode(String openAPI)
            throws EndpointCodeGeneratorMojoException {
        var logger = getLog();
        try {
            var executor = new GeneratorProcessor(project, logger)
                    .input(openAPI).verbose(logger.isDebugEnabled());

            generator.getOutputDir().ifPresent(executor::outputDir);
            generator.getPlugins().ifPresent(executor::plugins);

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new EndpointCodeGeneratorMojoException(
                    "TS code generation failed", e);
        }
    }

    private String parseJavaCode() throws EndpointCodeGeneratorMojoException {
        try {
            var executor = new ParserProcessor(project, getLog());

            parser.getClassPath().ifPresent(executor::classPath);
            parser.getEndpointAnnotation()
                    .ifPresent(executor::endpointAnnotation);
            parser.getPlugins().ifPresent(executor::plugins);
            parser.getOpenAPIPath().ifPresent(executor::openAPIBase);

            return executor.process();
        } catch (ParserException e) {
            throw new EndpointCodeGeneratorMojoException(
                    "Java code parsing failed", e);
        }
    }
}
