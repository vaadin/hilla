package dev.hilla.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "build-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_CLASSES)
@Execute(goal = "configure")
public class BuildFrontendMojo
        extends com.vaadin.flow.plugin.maven.BuildFrontendMojo {

    @Override
    protected boolean cleanFrontendFiles() {
        return false;
    }

}
