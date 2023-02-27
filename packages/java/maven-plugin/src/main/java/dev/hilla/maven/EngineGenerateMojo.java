package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.GeneratorException;
import dev.hilla.engine.GeneratorProcessor;
import dev.hilla.engine.ParserException;
import dev.hilla.engine.ParserProcessor;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(goal = "configure")
public final class EngineGenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "node")
    private String nodeCommand;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws EngineGenerateMojoException {
        try {
            var baseDir = project.getBasedir().toPath();
            var buildDir = baseDir.resolve(project.getBuild().getDirectory());
            var conf = Objects.requireNonNull(
                    EngineConfiguration.load(buildDir.toFile()));
            var classPath = conf.getClassPath();
            var urls = new ArrayList<URL>(classPath.size());
            for (var classPathItem : classPath) {
                urls.add(new File(classPathItem).toURI().toURL());
            }
            var classLoader = new URLClassLoader(urls.toArray(URL[]::new),
                    getClass().getClassLoader());
            var parserProcessor = new ParserProcessor(conf, classLoader);
            var generatorProcessor = new GeneratorProcessor(conf, nodeCommand);

            parserProcessor.process();
            generatorProcessor.process();
        } catch (IOException e) {
            throw new EngineGenerateMojoException(
                    "Loading saved configuration failed", e);
        } catch (GeneratorException | ParserException e) {
            throw new EngineGenerateMojoException("Execution failed", e);
        }
    }
}
