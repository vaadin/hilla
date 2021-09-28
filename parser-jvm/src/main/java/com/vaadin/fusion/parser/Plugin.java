package com.vaadin.fusion.parser;

public interface Plugin {
  void execute(RelativeClassList endpoints, RelativeClassList entities,
    SharedStorage storage);
}
