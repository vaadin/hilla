package dev.hilla.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "prepare-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Execute(goal = "configure")
public class PrepareFrontendMojo
        extends com.vaadin.flow.plugin.maven.PrepareFrontendMojo {
}
