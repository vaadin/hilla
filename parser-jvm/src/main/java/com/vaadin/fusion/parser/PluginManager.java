package com.vaadin.fusion.parser;

import java.util.List;
import java.util.function.Consumer;

class PluginManager {
  private final PluginManagerStore enters = new PluginManagerStore(Visit.Stage.Enter);
  private final PluginManagerStore exits = new PluginManagerStore(Visit.Stage.Exit);

  void add(Plugin plugin) {
    enters.add(plugin);
    exits.add(plugin);
  }

  void enter(Object node, Visit.Type type) {
    if (enters.has(type)) {
      apply(node, enters.get(type));
    }
  }

  void exit(Object node, Visit.Type type) {
    if (exits.has(type)) {
      apply(node, exits.get(type));
    }
  }

  private void apply(Object node, List<Consumer<Object>> list) {
    for (Consumer<Object> action : list) {
      action.accept(node);
    }
  }
}
