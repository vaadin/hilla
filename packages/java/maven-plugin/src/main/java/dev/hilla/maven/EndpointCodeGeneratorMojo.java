package dev.hilla.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EndpointCodeGeneratorMojo extends AbstractMojo {
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
        try {
            PluginConfiguration conf = new PluginConfiguration();
            conf.setBaseDir(project.getBasedir().toPath());
            conf.setClassPath(Stream
                    .of(project.getCompileClasspathElements(),
                            project.getRuntimeClasspathElements())
                    .flatMap(Collection::stream).collect(Collectors.toSet()));
            conf.setGenerator(generator);
            conf.setParser(parser);
            execute(conf);
        } catch (DependencyResolutionRequiredException ex) {
            throw new EndpointCodeGeneratorMojoException("Configuration failed",
                    ex);
        }
    }

    public void execute(PluginConfiguration conf)
            throws EndpointCodeGeneratorMojoException {
        var result = parseJavaCode(conf.getParser(), conf.getBaseDir(),
                conf.getClassPath());
        generateTypeScriptCode(result, conf.getGenerator(), conf.getBaseDir());
    }

    private void generateTypeScriptCode(String openAPI,
            GeneratorConfiguration generator, Path baseDir)
            throws EndpointCodeGeneratorMojoException {
        var logger = getLog();
        try {
            var executor = new GeneratorProcessor(baseDir, logger,
                    runNpmInstall).input(openAPI)
                            .verbose(logger.isDebugEnabled());

            generator.getOutputDir().ifPresent(executor::outputDir);
            generator.getPlugins().ifPresent(executor::plugins);

            executor.process();
        } catch (IOException | InterruptedException | GeneratorException e) {
            throw new EndpointCodeGeneratorMojoException(
                    "TS code generation failed", e);
        }
    }

    private String parseJavaCode(ParserConfiguration parser, Path baseDir,
            Set<String> classPath) throws EndpointCodeGeneratorMojoException {
        try {
            var executor = new ParserProcessor(baseDir, classPath, getLog());

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
