package com.vaadin.fusion.parser;

import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeSignature;

public final class Parser {
  private final String endpointAnnotationName;
  private final PluginManager manager = new PluginManager();
  private final ScanResult result;
  private final List<ClassInfo> standardClasses;

  public Parser(Configurator configurator) {
    this.endpointAnnotationName = configurator.getEndpointAnnotationName();
    result = new ClassGraph().enableAllInfo()
      .overrideClasspath(configurator.getClassPath())
      .scan();
    standardClasses = result.getAllStandardClasses();
  }

  private static List<ClassInfo> getClassChain(ClassInfo classInfo) {
    List<ClassInfo> superClasses = classInfo.getSuperclasses();
    List<ClassInfo> chain = new ArrayList<>(superClasses.size() + 1);
    chain.add(classInfo);
    chain.addAll(superClasses);
    return chain;
  }

  public Parser addPlugin(Plugin plugin) {
    manager.add(plugin);
    return this;
  }

  public void execute() {
    result.getClassesWithAnnotation(endpointAnnotationName)
      .forEach(cls -> process(cls, Visit.Type.EndpointClass));
    result.close();
  }

  private void process(AnnotationInfo element, Visit.Type type) {
    manager.enter(element, type);

    manager.exit(element, type);
  }

  private void process(ClassInfo element, Visit.Type type) {
    if (element == null) {
      return;
    }

    manager.enter(element, type);

    boolean isEndpoint = type == Visit.Type.EndpointClass;

    List<ClassInfo> classChain = getClassChain(element);
    classChain.stream()
      .flatMap(cls -> cls.getAnnotationInfo().stream())
      .forEach(annotation -> process(annotation,
        isEndpoint ? Visit.Type.EndpointAnnotation :
          Visit.Type.DataAnnotation));

    classChain.stream()
      .flatMap(cls -> cls.getMethodInfo().stream())
      .forEach(method -> process(method,
        isEndpoint ? Visit.Type.EndpointMethod : Visit.Type.DataMethod));

    classChain.stream()
      .flatMap(cls -> cls.getFieldInfo().stream())
      .forEach(field -> process(field,
        isEndpoint ? Visit.Type.EndpointField : Visit.Type.DataField));

    classChain.stream()
      .flatMap(cls -> cls.getInnerClasses().stream())
      .forEach(cls -> process(cls, Visit.Type.DataClass));

    manager.exit(element, type);
  }

  private void process(FieldInfo element, Visit.Type type) {
    manager.enter(element, type);

    element.getAnnotationInfo()
      .forEach(annotation -> process(annotation,
        type == Visit.Type.EndpointField ? Visit.Type.EndpointAnnotation :
          Visit.Type.DataAnnotation));

    TypeSignature signature = element.getTypeSignature();

    manager.exit(element, type);
  }

  private void process(MethodInfo element, Visit.Type type) {

  }

  private void resolve(TypeSignature type) {
    if (type instanceof ArrayTypeSignature) {
      if (!(((ArrayTypeSignature) type).getElementTypeSignature() instanceof BaseTypeSignature)) {
        return;
      }

      ClassInfo elementClassInfo =
        ((ArrayTypeSignature) type).getArrayClassInfo().getElementClassInfo();

      if (standardClasses.contains(elementClassInfo)) {

      }
    }
  }

  public static final class Configurator {
    private String classPath;
    private String endpointAnnotationName;

    public Configurator classPath(final String value) {
      this.classPath = value;

      return this;
    }

    public Configurator endpointAnnotationName(final String value) {
      this.endpointAnnotationName = value;

      return this;
    }

    public String getClassPath() {
      return classPath;
    }

    public String getEndpointAnnotationName() {
      return endpointAnnotationName;
    }
  }
}
