package dev.hilla.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import dev.hilla.parser.utils.OpenAPIPrinter;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.RUNTIME)
public final class EndpointCodeGeneratorMojo extends AbstractMojo {
    private static final String[] openAPIFileRelativePath = {
            "generated-resources", "openapi.json" };

    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();
    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "false")
    private boolean runNpmInstall;

    @Override
    public void execute() throws EndpointCodeGeneratorMojoException {
        var result = parseJavaCode();
        saveOpenAPI(result);
        generateTypeScriptCode(result);
    }

    private void generateTypeScriptCode(OpenAPI openAPI)
            throws EndpointCodeGeneratorMojoException {
        var logger = getLog();
        try {
            var executor = new GeneratorProcessor(project, logger,
                    runNpmInstall)
                            .input(new OpenAPIPrinter().writeAsString(openAPI))
                            .verbose(logger.isDebugEnabled());

            generator.getOutputDir().ifPresent(executor::outputDir);
            generator.getPlugins().ifPresent(executor::plugins);

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new EndpointCodeGeneratorMojoException(
                    "TS code generation failed", e);
        }
    }

    private void saveOpenAPI(OpenAPI openAPI)
            throws EndpointCodeGeneratorMojoException {
        try {
            var logger = getLog();
            var openAPIFile = Paths.get(project.getBuild().getDirectory(),
                    openAPIFileRelativePath);

            logger.debug("Saving OpenAPI file to " + openAPIFile);

            Files.createDirectories(openAPIFile.getParent());

            Files.write(openAPIFile, new OpenAPIPrinter().pretty()
                    .writeAsString(openAPI).getBytes());

            logger.debug("OpenAPI file saved");
        } catch (IOException e) {
            throw new EndpointCodeGeneratorMojoException(
                    "Saving OpenAPI file failed", e);
        }
    }

    private OpenAPI parseJavaCode() throws EndpointCodeGeneratorMojoException {
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
