package com.vaadin.fusion.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visit {
  Stage stage();
  Type type();

  enum Stage {
    Enter,
    Exit
  }

  enum Type {
    EndpointClass,
    EndpointField,
    EndpointMethod,
    EndpointAnnotation,
    EndpointFieldAnnotation,
    EndpointMethodAnnotation,
    DataClass,
    DataField,
    DataMethod,
    DataAnnotation,
    DataFieldAnnotation,
    DataMethodAnnotation,
    Type,
  }
}
