package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Objects;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.EngineException;
import dev.hilla.engine.EngineRunner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(goal = "configure")
public final class EngineGenerateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDirectory;

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
        }
    }
}
