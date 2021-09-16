package com.vaadin.fusion;

public class JVMParserMojoPlugin {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "JVMParserMojoPlugin { " +
      "name: " + name
      + "}";
  }
}
