package com.vaadin.fusion.parser;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Parser {
  private final SharedStorage storage = new SharedStorage();
  private String classPath;
  private String endpointAnnotationName;
  private Set<String> pluginClassNames;

  Parser() {
  }

  public Parser classPath(String value) {
    classPath = value;

    return this;
  }

  public Parser endpointAnnotationName(String value) {
    endpointAnnotationName = value;

    return this;
  }

  public void execute() {
    Objects.requireNonNull(classPath,
      "Fusion Parser: Classpath is not provided.");
    Objects.requireNonNull(pluginClassNames,
      "Fusion Parser: Plugins are not provided.");
    Objects.requireNonNull(endpointAnnotationName,
      "Fusion Parser: Endpoint annotation name is not provided.");

    PluginManager pluginManager = new PluginManager(pluginClassNames);

    ScanResult result = new ClassGraph().enableAllInfo()
      .overrideClasspath(classPath).scan();

    Collector collector = new Collector(result);

    pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
      storage);
  }

  public SharedStorage getStorage() {
    return storage;
  }

  public Parser pluginClassNames(Set<String> value) {
    pluginClassNames = value;

    return this;
  }

  private class Collector {
    private final RelativeClassList endpoints;
    private final RelativeClassList entities;

    Collector(ScanResult result) {
      endpoints = result.getClassesWithAnnotation(endpointAnnotationName)
        .stream().map(RelativeClassInfo::new)
        .collect(Collectors.toCollection(RelativeClassList::new));

      entities = endpoints.stream().flatMap(this::collectDependencies)
        .collect(Collectors.toCollection(RelativeClassList::new));

      for (int i = 0; i < entities.size(); i++) {
        RelativeClassInfo entity = entities.get(i);

        collectDependencies(entity).filter(
          dependency -> !entities.contains(dependency)).forEach(entities::add);
      }
    }

    RelativeClassList getEndpoints() {
      return endpoints;
    }

    RelativeClassList getEntities() {
      return entities;
    }

    private Stream<RelativeClassInfo> collectDependencies(
      RelativeClassInfo element) {
      return Stream.of(element.asStream().getFieldDependencies().unwrap(),
          element.asStream().getMethodDependencies().unwrap(),
          element.asStream().getInnerClassDependencies().unwrap(),
          element.asStream().getSuperDependencies().unwrap())
        .flatMap(Function.identity());
    }
  }
}
