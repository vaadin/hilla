package com.vaadin.hilla.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.hilla.engine.EngineConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.theme.Theme;

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
    @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
    private List<String> classpathElements;

    @Parameter(defaultValue = "${project.groupId}", readonly = true, required = true)
    private String groupId;

    @Parameter(defaultValue = "${project.artifactId}", readonly = true, required = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File buildDir;

    @Parameter(property = "spring-boot.aot.main-class")
    private String mainClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var conf = EngineConfiguration.getDefault();
        conf.setClasspath(classpathElements.stream().map(Path::of)
                .collect(Collectors.toSet()));
        conf.setGroupId(groupId);
        conf.setArtifactId(artifactId);
        conf.setMainClass(mainClass);
        conf.setBuildDir(buildDir.toPath());
        super.execute();
    }
}
