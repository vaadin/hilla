package com.vaadin.fusion.parser.core;

import java.util.List;

public interface Plugin {
    void execute(List<RelativeClassInfo> endpoints, List<RelativeClassInfo> entities,
                 SharedStorage storage);
}
