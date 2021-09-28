package com.vaadin.fusion.parser;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Parser {
  private String classPath;
  private String endpointAnnotationName;
  private Set<String> pluginClassNames;

  public Parser() {
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
    SharedStorage storage = new SharedStorage();

    ScanResult result =
      new ClassGraph().enableAllInfo().overrideClasspath(classPath).scan();

    Collector collector = new Collector(result);

    pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
      storage);
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
        .stream()
        .map(RelativeClassInfo::new)
        .collect(Collectors.toCollection(RelativeClassList::new));

      entities = endpoints.stream()
        .flatMap(endpoint -> Stream.concat(Stream.concat(
            Stream.concat(endpoint.asStream().getFieldDependencies().unwrap(),
              endpoint.asStream().getMethodDependencies().unwrap()),
            endpoint.asStream().getInnerClassDependencies().unwrap()),
          endpoint.asStream().getSuperDependencies().unwrap()))
        .collect(Collectors.toCollection(RelativeClassList::new));
    }

    RelativeClassList getEndpoints() {
      return endpoints;
    }

    RelativeClassList getEntities() {
      return entities;
    }
  }
}
