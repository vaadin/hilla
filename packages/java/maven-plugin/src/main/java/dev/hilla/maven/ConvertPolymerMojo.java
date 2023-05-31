package dev.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This is the `hilla:convert-polymer` goal which exists just for backward
 * compatibility purposes, and to warn the user about the incompatibility of its
 * functionality within Hilla applications.
 * <p>
 * NOTE: This goal is deprecated and will be removed in the future major
 * release.
 */
@Deprecated
@Mojo(name = "convert-polymer")
public class ConvertPolymerMojo
        extends com.vaadin.flow.plugin.maven.ConvertPolymerMojo {

    @Override
    public void execute() throws MojoFailureException {
        getLog().warn(
                """
                        The 'convert-polymer' goal is not meant to be used in Hilla projects as polymer templates are not supported.
                        Note: The 'convert-polymer' goal is deprecated and would be removed in future releases.
                        """
                        .stripIndent());
        super.execute();
    }
}
