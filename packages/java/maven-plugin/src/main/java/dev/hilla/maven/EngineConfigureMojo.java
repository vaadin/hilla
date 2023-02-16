package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorConfiguration;
import dev.hilla.engine.ParserConfiguration;
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

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The folder where TypeScript endpoints are generated.
     */
    @Parameter(defaultValue = "${project.basedir}/frontend/generated")
    private File generatedTsFolder;

    @Override
    public void execute() throws EngineConfigureMojoException {
        try {
            EngineConfiguration conf = new EngineConfiguration();
            conf.setClassPath(
                    new LinkedHashSet<>(project.getRuntimeClasspathElements()));

            conf.setBaseDir(project.getBasedir().toPath());
            conf.setOutputDir(generatedTsFolder.toPath());
            conf.setGenerator(generator);
            conf.setParser(parser);
            var buildDir = project.getBuild().getDirectory();
            conf.setBuildDir(buildDir);

            // The configuration gathered from the Maven plugin is saved in a
            // file so that further runs can skip running a separate Maven
            // project just to get this configuration again
            var configDir = project.getBasedir().toPath().resolve(buildDir);
            Files.createDirectories(configDir);
            conf.store(configDir.toFile());
        } catch (DependencyResolutionRequiredException e) {
            throw new EngineConfigureMojoException("Configuration failed", e);
        } catch (IOException e) {
            throw new EngineConfigureMojoException(
                    "Maven configuration has not been saved to file", e);
        }
    }
}
