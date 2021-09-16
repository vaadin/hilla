package com.vaadin.fusion.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.fusion.parser.utils.ExtendedHashMap;
import com.vaadin.fusion.parser.utils.ExtendedMap;

class PluginManagerStore {
  private final ExtendedMap<Visit.Type, List<Consumer<Object>>> storage =
    new ExtendedHashMap<>();
  private final Visit.Stage visitStage;

  PluginManagerStore(Visit.Stage visitStage) {
    this.visitStage = visitStage;
  }

  void add(Plugin plugin) {
    Method[] methods = plugin.getClass().getMethods();

    Arrays.stream(methods)
          .filter(method -> method.isAnnotationPresent(Visit.class) &&
            method.getAnnotation(Visit.class).stage() == visitStage)
          .forEach(method -> {
            Visit.Type nodeType = method.getAnnotation(Visit.class).type();

            List<Consumer<Object>> callbacks =
              storage.getOrCreateWithDefault(nodeType, ArrayList::new);

            callbacks.add((node) -> {
              try {
                method.invoke(plugin, node);
              } catch (IllegalAccessException | InvocationTargetException e) {
                throw new PluginException(String.format(
                  "'%s' plugin  failed trying to execute '%s' method",
                  plugin.getName(), method.getName()), e);
              }
            });
          });
  }

  boolean has(Visit.Type type) {
    return storage.containsKey(type);
  }

  List<Consumer<Object>> get(Visit.Type type) {
    return storage.get(type);
  }
}
