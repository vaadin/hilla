package com.vaadin.hilla.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This is the hidden `vaadin:dance` to clean up the frontend files.
 */
@Mojo(name = "dance", defaultPhase = LifecyclePhase.PRE_CLEAN)
public class FrontendDanceMojo
        extends com.vaadin.flow.plugin.maven.FrontendDanceMojo {

}
