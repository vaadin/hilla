package com.vaadin.fusion;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Fusion plugin for Maven. Handles loading the parser and its plugins.
 */
@Mojo(name = "fusion-generator", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class FusionGeneratorMojo extends AbstractMojo {
  @Parameter
  private Map<String, JVMParserMojoPlugin> plugins;
  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  private FusionJVMParser parser;

  public FusionGeneratorMojo() {
    parser = new FusionJVMParser(project.getCompileClasspathElements());
  }

  public void execute() throws MojoExecutionException {
    ClassLoader loader = getClass().getClassLoader();

    try {
      for (JVMParserMojoPlugin mojoPlugin : plugins.values()) {
        Class<ParserJVMPlugin> pluginClass = loader.loadClass(mojoPlugin.getName());
        ParserJVMPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
        parser.addPlugin(plugin);
      }

      parser.execute();
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new MojoExecutionException("Loading plugin is failed", e);
    }
  }
}
