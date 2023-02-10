package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Objects;

import dev.hilla.internal.EngineConfiguration;
import dev.hilla.internal.EngineException;
import dev.hilla.internal.EngineRunner;
import dev.hilla.internal.GeneratorConfiguration;
import dev.hilla.internal.GeneratorUnavailableException;
import dev.hilla.internal.ParserConfiguration;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
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
@Execute(goal = "configure")
public final class EngineGenerateMojo extends AbstractMojo {

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
    public void execute() throws EngineGenerateMojoException {
        try {
            var conf = Objects.requireNonNull(
                EngineConfiguration.load(buildDirectory));
            var classPath = conf.getClassPath();
            var urls = new ArrayList<URL>(classPath.size());
            for (var classPathItem : classPath) {
                urls.add(new File(classPathItem).toURI().toURL());
            }
            var classLoader = new URLClassLoader(urls.toArray(URL[]::new));
            new EngineRunner(conf, classLoader).execute();
        } catch (IOException e) {
            throw new EngineGenerateMojoException(
                    "Loading saved configuration failed", e);
        } catch (EngineException e) {
            throw new EngineGenerateMojoException("Execution failed", e);
        } catch (GeneratorUnavailableException e) {
            if (failOnMissingGenerator) {
                throw new EngineGenerateMojoException("Execution failed", e);
            } else {
                getLog().warn(e.getMessage());
            }
        }
    }
}
