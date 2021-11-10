package com.vaadin.fusion.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;

final class PluginManager {
    private static final List<Class<? extends Plugin>> builtInPlugins = Arrays
            .asList(BackbonePlugin.class);
    private static final ClassLoader loader = PluginManager.class
            .getClassLoader();

    private final Set<Plugin> plugins = new LinkedHashSet<>();

    PluginManager(ParserConfig config) {
        Set<String> disabledPluginNames = config.getPlugins().getDisable();
        Stream<Class<? extends Plugin>> activeBuiltInPlugins = builtInPlugins
                .stream().filter(plugin -> !disabledPluginNames
                        .contains(plugin.getName()));

        Stream<Class<? extends Plugin>> userDefinedPlugins = config.getPlugins()
                .getUse().stream().map(name -> processClass(loadClass(name)));

        Stream.concat(activeBuiltInPlugins, userDefinedPlugins)
                .map(this::instantiatePlugin)
                .collect(Collectors.toCollection(() -> plugins));
    }

    public void execute(List<RelativeClassInfo> endpoints,
            List<RelativeClassInfo> entities, SharedStorage storage) {
        for (Plugin plugin : plugins) {
            plugin.execute(endpoints, entities, storage);
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ParserException(String.format(
                    "Plugin '%s' is not found in the classpath", className), e);
        }
    }

    private Class<? extends Plugin> processClass(Class<?> cls) {
        if (Plugin.class.isAssignableFrom(cls)) {
            return (Class<? extends Plugin>) cls;
        }

        throw new ParserException(String.format(
                "Plugin '%s' is not an instance of '%s' interface",
                cls.getName(), Plugin.class.getName()));
    }

    private Plugin instantiatePlugin(Class<? extends Plugin> pluginClass) {
        try {
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new ParserException(
                    String.format("Cannot instantiate plugin '%s'",
                            pluginClass.getName()),
                    e);
        }
    }
}
