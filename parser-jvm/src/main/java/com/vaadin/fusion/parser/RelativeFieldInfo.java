package com.vaadin.fusion.parser;

import io.github.classgraph.FieldInfo;

import static com.vaadin.fusion.parser.Resolver.resolveAsRelative;

public class RelativeFieldInfo implements Relative {
  private final StreamAPI streamAPI = new StreamAPI();
  private final FieldInfo fieldInfo;

  RelativeFieldInfo(final FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
  }

  @Override
  public StreamAPI asStream() {
    return streamAPI;
  }

  @Override
  public FieldInfo get() {
    return fieldInfo;
  }

  public RelativeClassList getDependencies() {
    return streamAPI.getDependencies().collectToList();
  }

  public final class StreamAPI {
    public RelativeClassStream getDependencies() {
      return resolveAsRelative(fieldInfo.getTypeSignature());
    }
  }
}
