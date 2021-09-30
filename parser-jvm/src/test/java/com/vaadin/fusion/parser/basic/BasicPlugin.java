package com.vaadin.fusion.parser.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.fusion.parser.Plugin;
import com.vaadin.fusion.parser.RelativeClassInfo;
import com.vaadin.fusion.parser.RelativeClassList;
import com.vaadin.fusion.parser.SharedStorage;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class BasicPlugin implements Plugin {
  public static final String STORAGE_KEY = "BasicPluginResult";

  @Override
  public void execute(RelativeClassList endpoints, RelativeClassList entities,
    SharedStorage storage) {
    List<String> endpointMemberNames = new ArrayList<>();

    for (RelativeClassInfo endpoint : endpoints) {
      Stream.of(endpoint.get().getFieldInfo().stream().map(FieldInfo::getName),
          endpoint.get().getMethodInfo().stream().map(MethodInfo::getName),
          endpoint.get().getInnerClasses().stream().map(ClassInfo::getName))
        .reduce(Stream::concat).orElseGet(Stream::empty)
        .forEach(endpointMemberNames::add);
    }

    storage.getPluginStorage().put(STORAGE_KEY, endpointMemberNames);
  }
}
