package com.vaadin.hilla.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Goal that generates a CycloneDX SBOM file focused on backend dependencies.
 */
@Mojo(name = "generate-maven-sbom", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenerateMavenBOMMojo
        extends com.vaadin.flow.plugin.maven.GenerateMavenBOMMojo {
}
