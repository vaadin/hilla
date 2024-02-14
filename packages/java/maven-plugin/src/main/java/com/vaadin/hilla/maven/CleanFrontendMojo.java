package com.vaadin.hilla.maven;

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
}
