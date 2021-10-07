package com.vaadin.fusion.parser.core;

public interface Plugin {
    void execute(RelativeClassList endpoints, RelativeClassList entities,
            SharedStorage storage);
}
