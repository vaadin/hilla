package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.internal.EngineConfiguration;
import dev.hilla.internal.GeneratorConfiguration;
import dev.hilla.internal.ParserConfiguration;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Maven Plugin for Hilla. Emits Hilla engine configuration file in the build
 * directory.
 *
 * The configuration gathered from the Maven plugin is saved in a file, so that
 * further runs of the parser / generator can skip running a separate Maven
 * process to get this configuration again.
 */
@Mojo(name = "configure", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EngineConfigureMojo extends AbstractMojo {

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
    public void execute() throws EngineConfigureMojoException {
        try {
            EngineConfiguration conf = new EngineConfiguration();
            conf.setClassPath(Stream
                    .of(project.getCompileClasspathElements(),
                            project.getRuntimeClasspathElements())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));

            conf.setBaseDir(project.getBasedir().toPath());
            conf.setGenerator(generator);
            conf.setParser(parser);
            var buildDir = project.getBuild().getDirectory();
            conf.setBuildDir(buildDir);

            // The configuration gathered from the Maven plugin is saved in a
            // file
            // so that further runs can skip running a separate Maven project
            // just
            // to get this configuration again
            Files.createDirectories(Paths.get(buildDir));
            conf.store(buildDirectory);
        } catch (DependencyResolutionRequiredException e) {
            throw new EngineConfigureMojoException("Configuration failed", e);
        } catch (IOException e) {
            throw new EngineConfigureMojoException(
                    "Maven configuration has not been saved to file", e);
        }
    }
}
