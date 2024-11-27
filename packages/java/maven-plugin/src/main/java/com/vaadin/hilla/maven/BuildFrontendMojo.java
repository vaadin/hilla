package com.vaadin.hilla.maven;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.theme.Theme;
import com.vaadin.hilla.engine.EngineConfiguration;

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
    @Parameter(property = "mainClass")
    private String mainClass;

    @Parameter(property = "endpointProvider")
    private String endpointProvider;

    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        var project = (MavenProject) getPluginContext().get("project");
        if (project == null) {
            throw new MojoExecutionException("No project found");
        }
        if (mainClass == null) {
            mainClass = getSpringBootMainClass(project.getBuildPlugins());
        }
        EngineConfiguration.setDefault(new EngineConfiguration.Builder()
                .baseDir(npmFolder().toPath()).buildDir(buildFolder())
                .outputDir(generatedTsFolder().toPath())
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .offlineEndpointProvider(createEndpointProvider())
                .classpath(getClasspathElements(project)).mainClass(mainClass)
                .create());
        super.executeInternal();
    }

    private EngineConfiguration.EndpointProvider createEndpointProvider() {
        try {
            System.out.println("endpointProvider: " + endpointProvider);
            return endpointProvider == null ? null
                    : (EngineConfiguration.EndpointProvider) Class
                            .forName(endpointProvider).getDeclaredConstructor()
                            .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create endpoint provider", e);
        }
    }

    private String getSpringBootMainClass(List<Plugin> plugins) {
        return plugins.stream().filter(plugin -> "org.springframework.boot"
                .equals(plugin.getGroupId())
                && "spring-boot-maven-plugin".equals(plugin.getArtifactId()))
                .findFirst().map(plugin -> {
                    var configuration = (Xpp3Dom) plugin.getConfiguration();
                    if (configuration != null) {
                        var mainClassNode = configuration.getChild("mainClass");
                        if (mainClassNode != null) {
                            return mainClassNode.getValue();
                        }
                    }
                    return null;
                }).orElse(null);
    }
}
