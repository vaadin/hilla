package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mojo(name = "gather-configuration", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ConfigurationMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(readonly = true)
    private final GeneratorConfiguration generator = new GeneratorConfiguration();

    @Parameter(readonly = true)
    private final ParserConfiguration parser = new ParserConfiguration();

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDirectory;

    private static Set<String> classPath;
    private static Path baseDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        baseDir = project.getBasedir().toPath();

        try {
            classPath = Stream
                    .of(project.getCompileClasspathElements(),
                            project.getRuntimeClasspathElements())
                    .flatMap(Collection::stream).collect(Collectors.toSet());
        } catch (DependencyResolutionRequiredException ex) {
            throw new EndpointCodeGeneratorMojoException(
                    "Maven classPath is not readable", ex);
        }

        PluginConfiguration c = new PluginConfiguration();
        c.setBaseDir(baseDir);
        c.setClassPath(classPath);
        c.setGenerator(generator);
        c.setParser(parser);

        buildDirectory.mkdirs();
        try {
            var mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL,
                    JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD,
                    JsonAutoDetect.Visibility.ANY);
            c.store(buildDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

}
