package com.vaadin.hilla.maven;

import java.io.File;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.hilla.engine.EngineConfiguration;

import static com.vaadin.flow.plugin.maven.FlowModeAbstractMojo.getClasspathElements;
import static com.vaadin.flow.server.frontend.FrontendUtils.FRONTEND;
import static com.vaadin.flow.server.frontend.FrontendUtils.GENERATED;

/**
 * This goal is no longer used, so invoking it will only print a warning.
 */
@Mojo(name = "configure", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EngineConfigureMojo extends AbstractMojo {
    /**
     * A directory with project's frontend source files.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/" + FRONTEND)
    private File frontendDirectory;

    /**
     * The folder where TypeScript endpoints are generated.
     */
    @Parameter
    private File generatedTsFolder;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "node")
    private String nodeCommand;

    @Parameter(property = "mainClass")
    private String mainClass;

    @Override
    public void execute() throws MojoFailureException {
        var isProduction = project.getActiveProfiles().stream()
                .map(Profile::getId).anyMatch("production"::equals);
        var conf = new EngineConfiguration.Builder()
                .baseDir(project.getBasedir().toPath())
                .buildDir(project.getBuild().getDirectory())
                .outputDir(generatedTsFolder().toPath())
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .classpath(getClasspathElements(project)).mainClass(mainClass)
                .nodeCommand(nodeCommand).productionMode(isProduction).create();
        EngineConfiguration.setDefault(conf);
    }

    private File generatedTsFolder() {
        if (generatedTsFolder != null) {
            return generatedTsFolder;
        }
        return new File(frontendDirectory, GENERATED);
    }
}
