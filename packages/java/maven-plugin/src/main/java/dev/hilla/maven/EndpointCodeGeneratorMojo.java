package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.maven.runner.GeneratorConfiguration;
import dev.hilla.maven.runner.GeneratorUnavailableException;
import dev.hilla.maven.runner.ParserConfiguration;
import dev.hilla.maven.runner.PluginConfiguration;
import dev.hilla.maven.runner.PluginException;
import dev.hilla.maven.runner.PluginRunner;

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

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDirectory;

    // If set to false, the plugin will not fail if the generator is not
    // available. This allows to run this goal just to save the configuration,
    // without having to run 'npm install' to avoid an error
    @Parameter(property = "hilla.failOnMissingGenerator", defaultValue = "true")
    private boolean failOnMissingGenerator;

    @Override
    public void execute() throws EndpointCodeGeneratorMojoException {
        PluginConfiguration conf = new PluginConfiguration();

        try {
            conf.setClassPath(Stream
                    .of(project.getCompileClasspathElements(),
                            project.getRuntimeClasspathElements())
                    .flatMap(Collection::stream).collect(Collectors.toSet()));
        } catch (DependencyResolutionRequiredException e) {
            throw new EndpointCodeGeneratorMojoException("Configuration failed",
                    e);
        }

        conf.setBaseDir(project.getBasedir().toPath());
        conf.setGenerator(generator);
        conf.setParser(parser);
        var buildDir = project.getBuild().getDirectory();
        conf.setBuildDir(buildDir);

        // The configuration gathered from the Maven plugin is saved in a file
        // so that further runs can skip running a separate Maven project just
        // to get this configuration again
        try {
            Files.createDirectories(Paths.get(buildDir));
            conf.store(buildDirectory);
        } catch (IOException e) {
            getLog().warn("Maven configuration has not been saved to file", e);
        }

        try {
            new PluginRunner(conf).execute();
        } catch (PluginException e) {
            throw new EndpointCodeGeneratorMojoException("Execution failed", e);
        } catch (GeneratorUnavailableException e) {
            if (failOnMissingGenerator) {
                throw new EndpointCodeGeneratorMojoException("Execution failed",
                        e);
            } else {
                getLog().warn(e.getMessage());
            }
        }
    }
}
