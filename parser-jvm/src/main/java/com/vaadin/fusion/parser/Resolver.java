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
  static Stream<ClassInfo> resolve(TypeSignature type) {
    if (type == null) {
      return Stream.empty();
    }

    return type instanceof BaseTypeSignature ?
        resolveAbstract((BaseTypeSignature) type) :
        resolveAbstract((ReferenceTypeSignature) type);
  }

  static Stream<ClassInfo> resolve(Stream<TypeSignature> types) {
    return types.flatMap(Resolver::resolve);
  }

  static RelativeClassStream resolveAsRelative(TypeSignature type) {
    return new RelativeClassStream(resolve(type).map(RelativeClassInfo::new));
  }

  static RelativeClassStream resolveAsRelative(Stream<TypeSignature> types) {
    return new RelativeClassStream(resolve(types).map(RelativeClassInfo::new));
  }

  // Primitive type (int, double, etc.). We don't need to resolve it, so
  // skipping.
  private static Stream<ClassInfo> resolveAbstract(BaseTypeSignature type) {
    return Stream.empty();
  }

  private static Stream<ClassInfo> resolveAbstract(ReferenceTypeSignature type) {
    if (type instanceof ArrayTypeSignature) {
      return resolveSpecific((ArrayTypeSignature) type);
    } else if (type instanceof TypeVariableSignature) {
      return resolveSpecific((TypeVariableSignature) type);
    }

    return resolveSpecific((ClassRefTypeSignature) type);
  }

  // SomeType[]. Resolving the array element.
  private static Stream<ClassInfo> resolveSpecific(ArrayTypeSignature type) {
    return resolve(type.getElementTypeSignature());
  }

  private static Stream<ClassInfo> resolveSpecific(TypeVariableSignature type) {
    // We can resolve only the type variable class bound here (class bound is
    // `com.vaadin.fusion.X` in `T extends com.vaadin.fusion.X`)
    TypeSignature bound = type.resolve().getClassBound();

    return bound != null ? resolve(bound) : Stream.empty();
  }

  private static Stream<ClassInfo> resolveSpecific(ClassRefTypeSignature type) {
    ClassInfo classInfo = type.getClassInfo();

    // All native class refs (like List<>, Set<>, etc., are null). So if it is
    // not null, we can resolve it directly.
    return classInfo != null ? Stream.of(classInfo) : type.getTypeArguments()
        .stream()
        .flatMap(argument -> resolve(argument.getTypeSignature()));
  }
}
