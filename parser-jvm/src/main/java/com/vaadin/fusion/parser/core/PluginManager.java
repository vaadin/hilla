package com.vaadin.fusion.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

class PluginManager {
    private final Set<Plugin> plugins;

    PluginManager(ParserConfig config) {
        ClassLoader loader = getClass().getClassLoader();
        plugins = config.getPlugins().getUse().stream().map(name -> {
            try {
                return ((Class<Plugin>) loader.loadClass(name))
                        .getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException
                    | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new ParserException(
                        String.format("Cannot instantiate plugin '%s'", name),
                        e);
            }
        }).collect(Collectors.toSet());
    }

    void execute(RelativeClassList endpoints, RelativeClassList entities,
            SharedStorage storage) {
        for (Plugin plugin : plugins) {
            plugin.execute(endpoints, entities, storage);
        }
    }
}
