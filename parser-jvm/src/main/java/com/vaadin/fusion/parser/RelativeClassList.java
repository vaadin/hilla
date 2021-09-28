package com.vaadin.fusion.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class RelativeClassList extends ArrayList<RelativeClassInfo> {
  public List<RelativeAnnotationInfo> getAnnotations() {
    return streamRelative().getAnnotations().collect(Collectors.toList());
  }

  public List<RelativeAnnotationInfo> getAnnotations(
    Predicate<AnnotationInfo> condition) {
    return streamRelative().getAnnotations(condition)
      .collect(Collectors.toList());
  }

  public List<RelativeFieldInfo> getFields() {
    return streamRelative().getFields().collect(Collectors.toList());
  }

  public List<RelativeFieldInfo> getFields(Predicate<FieldInfo> condition) {
    return streamRelative().getFields(condition).collect(Collectors.toList());
  }

  public RelativeClassList getInnerClasses() {
    return streamRelative().getInnerClasses().collectToList();
  }

  public RelativeClassList getInnerClasses(Predicate<ClassInfo> condition) {
    return streamRelative().getInnerClasses(condition).collectToList();
  }

  public List<RelativeMethodInfo> getMethods() {
    return streamRelative().getMethods().collect(Collectors.toList());
  }

  public List<RelativeMethodInfo> getMethods(Predicate<MethodInfo> condition) {
    return streamRelative().getMethods(condition)
      .collect(Collectors.toList());
  }

  public RelativeClassStream streamRelative() {
    return new RelativeClassStream(stream());
  }
}
