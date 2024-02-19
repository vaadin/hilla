package com.vaadin.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.plugin.maven.FlowModeAbstractMojo;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.GeneratorConfiguration;
import com.vaadin.hilla.engine.ParserConfiguration;

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
    /**
     * The folder where TypeScript endpoints are generated.
     */
    @Parameter(defaultValue = "${project.basedir}/frontend/generated")
    private File generatedTsFolder;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws EngineConfigureMojoException {

        if (!FlowModeAbstractMojo.isHillaAvailable(project)) {
            getLog().warn(
                    """
                            The 'configure' goal is only meant to be used in Hilla projects with endpoints.
                            """
                            .stripIndent());
            return;
        }
        try {
            var buildDir = project.getBuild().getDirectory();
            var conf = new EngineConfiguration.Builder(
                    project.getBasedir().toPath())
                    .classPath(new LinkedHashSet<>(
                            project.getRuntimeClasspathElements()))
                    .outputDir(generatedTsFolder.toPath()).generator(generator)
                    .parser(parser).buildDir(buildDir)
                    .classesDir(project.getBuild().getOutputDirectory())
                    .create();

            // The configuration gathered from the Maven plugin is saved in a
            // file so that further runs can skip running a separate Maven
            // project just to get this configuration again
            var configDir = project.getBasedir().toPath().resolve(buildDir);
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
    }
}
