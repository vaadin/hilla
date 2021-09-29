package com.vaadin.fusion.parser;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.vaadin.fusion.parser.Resolver.resolveAsRelative;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;

public class RelativeMethodInfo implements Relative {
  private final MethodInfo methodInfo;
  private final StreamAPI streamAPI = new StreamAPI();

  RelativeMethodInfo(final MethodInfo methodInfo) {
    this.methodInfo = methodInfo;
  }

  @Override
  public StreamAPI asStream() {
    return streamAPI;
  }

  @Override
  public MethodInfo get() {
    return methodInfo;
  }

  public RelativeClassList getDependencies() {
    return streamAPI.getDependencies().collectToList();
  }

  public RelativeClassList getParameterDependencies() {
    return streamAPI.getParameterDependencies().collectToList();
  }

  public RelativeClassList getResultDependencies() {
    return streamAPI.getResultDependencies().collectToList();
  }

  public final class StreamAPI {
    public RelativeClassStream getDependencies() {
      return new RelativeClassStream(
        Stream.concat(getResultDependencies().unwrap(),
          getParameterDependencies().unwrap()));
    }

    public RelativeClassStream getParameterDependencies() {
      return resolveAsRelative(Arrays.stream(methodInfo.getParameterInfo())
        .map(MethodParameterInfo::getTypeSignature));
    }

    public RelativeClassStream getResultDependencies() {
      return resolveAsRelative(methodInfo.getTypeSignature().getResultType());
    }
  }
}
