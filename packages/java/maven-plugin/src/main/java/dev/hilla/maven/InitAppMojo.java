package dev.hilla.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import dev.hilla.plugin.base.InitFileExtractor;

@Mojo(name = "init-app", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class InitAppMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}")
    private File projectBaseDir;

    @Override
    public void execute() throws MojoFailureException {
        var extractor = new InitFileExtractor(projectBaseDir.toPath());

        try {
            extractor.execute();
        } catch (IOException e) {
            throw new MojoFailureException(e);
        }
    }
}
