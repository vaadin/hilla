package com.vaadin.fusion.parser;

import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ReferenceTypeSignature;
import io.github.classgraph.TypeSignature;
import io.github.classgraph.TypeVariableSignature;

class Resolver {
  Stream<ClassInfo> resolve(TypeSignature type) {
    if (type instanceof BaseTypeSignature) {
      return resolveAbstract((BaseTypeSignature) type);
    }

    return resolveAbstract((ReferenceTypeSignature) type);
  }

  // Primitive type (int, double, etc.). We don't need to resolve it, so
  // skipping.
  private Stream<ClassInfo> resolveAbstract(BaseTypeSignature type) {
    return Stream.empty();
  }

  private Stream<ClassInfo> resolveAbstract(ReferenceTypeSignature type) {
    if (type instanceof ArrayTypeSignature) {
      return resolveSpecific((ArrayTypeSignature) type);
    } else if (type instanceof TypeVariableSignature) {
      return resolveSpecific((TypeVariableSignature) type);
    }

    return resolveSpecific((ClassRefTypeSignature) type);
  }

  // SomeType[]. Resolving the array element.
  private Stream<ClassInfo> resolveSpecific(ArrayTypeSignature type) {
    return resolve(type.getElementTypeSignature());
  }

  private Stream<ClassInfo> resolveSpecific(TypeVariableSignature type) {

  }

  private Stream<ClassInfo> resolveSpecific(ClassRefTypeSignature type) {
    ClassInfo classInfo = type.getClassInfo();

    // All native class refs (like List<>, Set<>, etc., are null). So if it is
    // not null, we can resolve it directly.
    if (classInfo != null) {
      return Stream.of(classInfo);
    }

    return type.getTypeArguments()
      .stream()
      .flatMap(argument -> resolve(argument.getTypeSignature()));
  }
}
