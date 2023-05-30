package dev.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This is the `hilla:clean-frontend` goal which exists just for backward
 * compatibility purposes, and to warn the user about the incompatibility of its
 * functionality within Hilla applications.
 * <p>
 * NOTE: This goal is deprecated and will be removed in the future major
 * release.
 */
@Deprecated
@Mojo(name = "clean-frontend", defaultPhase = LifecyclePhase.PRE_CLEAN)
public class CleanFrontendMojo
        extends com.vaadin.flow.plugin.maven.CleanFrontendMojo {

    @Override
    public void execute() throws MojoFailureException {
        getLog().warn(
                """
                        The 'clean-frontend' goal is not meant to be used in Hilla projects as it delete 'package-lock.json' and also clearing out the content of 'package.json'.
                        Note: The 'clean-frontend' goal is deprecated and would be removed in future releases.
                        """
                        .stripIndent());
        super.execute();
    }
}
