package com.vaadin.hilla.maven;

import com.vaadin.hilla.engine.ParserConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.List;

/**
 * This goal checks that node and npm tools are installed and creates or updates
 * `package.json` and the frontend build tool configuration files.
 * <p>
 * Copies frontend resources available inside `.jar` dependencies to
 * `node_modules` when building a jar package.
 *
 * @since Flow 2.0
 */
@Mojo(name = "prepare-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PrepareFrontendMojo
        extends com.vaadin.flow.plugin.maven.PrepareFrontendMojo {
    @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
    private List<String> classpathElements;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ParserConfiguration.CLASSPATH = classpathElements;
        super.execute();
    }
}
