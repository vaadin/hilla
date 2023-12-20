package com.vaadin.hilla.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "build-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_CLASSES)
@Execute(goal = "configure")
public class BuildFrontendMojo
        extends com.vaadin.flow.plugin.maven.BuildFrontendMojo {
    /**
     * Override this to not clean generated frontend files after the build. For
     * Hilla, the generated files can still be useful for developers after the
     * build. For example, a developer can use {@code vite.generated.ts} to run
     * tests with vitest in CI.
     *
     * @return {@code false}
     */
    @Override
    protected boolean cleanFrontendFiles() {
        return false;
    }
}
