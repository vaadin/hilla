package com.vaadin.hilla.typescript.parser.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface PluginConfiguration {
}
