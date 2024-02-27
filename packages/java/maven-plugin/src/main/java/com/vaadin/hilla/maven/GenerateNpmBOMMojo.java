package com.vaadin.hilla.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Goal that generates a CycloneDX SBOM file focused on frontend dependencies.
 */
@Mojo(name = "generate-npm-sbom", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenerateNpmBOMMojo
        extends com.vaadin.flow.plugin.maven.GenerateNpmBOMMojo {
}
