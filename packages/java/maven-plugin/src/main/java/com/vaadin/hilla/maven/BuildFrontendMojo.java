package com.vaadin.hilla.maven;

import com.vaadin.hilla.engine.EngineConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.theme.Theme;
import org.apache.maven.project.MavenProject;

/**
 * Goal that builds the frontend bundle.
 *
 * It performs the following actions when creating a package:
 * <ul>
 * <li>Update {@link Constants#PACKAGE_JSON} file with the {@link NpmPackage}
 * annotations defined in the classpath,</li>
 * <li>Copy resource files used by flow from `.jar` files to the `node_modules`
 * folder</li>
 * <li>Install dependencies by running <code>npm install</code></li>
 * <li>Update the {@link FrontendUtils#IMPORTS_NAME} file imports with the
 * {@link JsModule} {@link Theme} and {@link JavaScript} annotations defined in
 * the classpath,</li>
 * <li>Update {@link FrontendUtils#VITE_CONFIG} file.</li>
 * </ul>
 *
 * @since Flow 2.0
 */
@Mojo(name = "build-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class BuildFrontendMojo
        extends com.vaadin.flow.plugin.maven.BuildFrontendMojo {
    // FIXME(platosha): Maven only supports parameters on a single class.
    // @Parameter(property = "spring-boot.aot.main-class")
    // private String mainClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var project = (MavenProject) getPluginContext().get("project");
        if (project == null) {
            throw new MojoExecutionException("No project found");
        }
        EngineConfiguration.setDefault(new EngineConfiguration.Builder()
                .baseDir(npmFolder().toPath()).buildDir(buildFolder())
                .outputDir(generatedTsFolder().toPath())
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                // .mainClass(mainClass)
                .create());
        super.execute();
    }
}
