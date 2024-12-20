package com.vaadin.hilla.maven;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.vaadin.hilla.engine.EngineConfiguration;

import static com.vaadin.flow.plugin.maven.FlowModeAbstractMojo.getClasspathElements;
import static com.vaadin.flow.server.frontend.FrontendUtils.GENERATED;

interface Configurable {
    Map getPluginContext();

    File getFrontend();

    File getGenerated();

    String getNode();

    String getMainClass();

    default EngineConfiguration configure() {
        var project = (MavenProject) getPluginContext().get("project");

        var isProduction = project.getActiveProfiles().stream()
                .map(Profile::getId).anyMatch("production"::equals);

        var mainClass = getMainClass();
        if (mainClass == null) {
            mainClass = getPluginConfigurationProperty(project,
                    "org.springframework.boot", "spring-boot-maven-plugin",
                    "mainClass");
        }

        var conf = new EngineConfiguration.Builder()
                .baseDir(project.getBasedir().toPath())
                .buildDir(project.getBuild().getDirectory())
                .outputDir(generatedOrOldLocation().toPath())
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .classpath(getClasspathElements(project))
                .withDefaultAnnotations().mainClass(mainClass)
                .nodeCommand(getNode()).productionMode(isProduction).build();
        EngineConfiguration.setDefault(conf);
        return conf;
    }

    private File generatedOrOldLocation() {
        if (getGenerated() != null) {
            return getGenerated();
        }
        return new File(getFrontend(), GENERATED);
    }

    /**
     * Retrieves a configuration property value from another plugin.
     *
     * @param project
     *            the MavenProject instance
     * @param pluginGroupId
     *            the groupId of the target plugin
     * @param pluginArtifactId
     *            the artifactId of the target plugin
     * @param propertyName
     *            the name of the property to retrieve
     * @return the value of the property, or null if not found
     */
    static String getPluginConfigurationProperty(MavenProject project,
            String pluginGroupId, String pluginArtifactId,
            String propertyName) {
        // Search for the plugin in the project's build plugins
        for (var plugin : project.getBuild().getPlugins()) {
            if (pluginGroupId.equals(plugin.getGroupId())
                    && pluginArtifactId.equals(plugin.getArtifactId())) {
                // Access the plugin's configuration
                var configuration = plugin.getConfiguration();
                if (configuration instanceof Xpp3Dom configDom) {
                    var propertyNode = configDom.getChild(propertyName);
                    return Optional.ofNullable(propertyNode)
                            .map(Xpp3Dom::getValue)
                            // need to filter for property names in value
                            .filter(v -> v.indexOf('{') < 0).orElse(null);
                }
            }
        }

        // Plugin or property not found
        return null;
    }
}
