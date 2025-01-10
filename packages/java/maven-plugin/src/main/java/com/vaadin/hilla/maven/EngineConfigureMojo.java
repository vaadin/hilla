package com.vaadin.hilla.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * This goal is no longer used, so invoking it will only print a warning.
 */
@Mojo(name = "configure", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Deprecated
public final class EngineConfigureMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoFailureException {
        getLog().warn(
                """
                        The 'configure' goal is no longer used and will be removed in a future version.
                        """
                        .stripIndent());
    }
}
