package dev.hilla.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import dev.hilla.plugin.base.HillaAppInitUtility;
import org.apache.maven.project.MavenProject;

@Mojo(name = "init-app", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class InitAppMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}")
    private File projectBaseDir;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Override
    public void execute() throws MojoFailureException {
        try {
            List<String> dependencyArtifactIds = project.getDependencies()
                    .stream()
                    .filter(dependency -> !dependency.isOptional()
                            && !dependency.getScope().equals("test"))
                    .map(Dependency::getArtifactId).toList();
            HillaAppInitUtility.scaffold(projectBaseDir.toPath(),
                    dependencyArtifactIds);
        } catch (Exception e) {
            throw new MojoFailureException(e);
        }
    }
}
