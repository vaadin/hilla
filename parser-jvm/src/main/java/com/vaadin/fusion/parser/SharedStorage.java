package com.vaadin.fusion.parser;

import java.util.HashMap;
import java.util.Map;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public class SharedStorage {
  private final OpenApi3 openAPI = new OpenApi3Parser().parse("{}", null);
  private final Map<String, Object> pluginStorage = new HashMap<>();

  SharedStorage() {}

  public OpenApi3 getOpenAPI() {
    return openAPI;
  }

  public Map<String, Object> getPluginStorage() {
    return pluginStorage;
  }
}
