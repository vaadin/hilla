package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorConfiguration;
import dev.hilla.engine.ParserConfiguration;

@Mojo(name = "prepare-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PrepareFrontendMojo
        extends com.vaadin.flow.plugin.maven.PrepareFrontendMojo {
    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();

    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();

    // This has been renamed to not hide <code>project</code> from the ancestor
    // class, otherwise that's not given a value
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            var buildDir = mavenProject.getBuild().getDirectory();
            var conf = new EngineConfiguration.Builder(
                    mavenProject.getBasedir().toPath())
                            .classPath(new LinkedHashSet<>(
                                    mavenProject.getRuntimeClasspathElements()))
                            .outputDir(generatedTsFolder().toPath())
                            .generator(generator).parser(parser)
                            .buildDir(buildDir).classesDir(mavenProject
                                    .getBuild().getOutputDirectory())
                            .create();

            // The configuration gathered from the Maven plugin is saved in a
            // file so that further runs can skip running a separate Maven
            // mavenProject just to get this configuration again
            var configDir = mavenProject.getBasedir().toPath()
                    .resolve(buildDir);
            Files.createDirectories(configDir);
            conf.store(configDir
                    .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME)
                    .toFile());
        } catch (DependencyResolutionRequiredException e) {
            throw new EngineConfigureMojoException("Configuration failed", e);
        } catch (IOException e) {
            throw new EngineConfigureMojoException(
                    "Maven configuration has not been saved to file", e);
        }

        super.execute();
    }

}
