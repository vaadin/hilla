package com.vaadin.fusion.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PluginManager {
    private static final ClassLoader loader = PluginManager.class
        .getClassLoader();
    private static final Logger logger = LoggerFactory
        .getLogger(PluginManager.class);

    private final Set<Plugin> plugins;

    PluginManager(ParserConfig config) {
        plugins = config.getPlugins().stream()
            .map(this::loadClass).map(this::processClass).map(this::instantiatePlugin)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void execute(List<RelativeClassInfo> endpoints,
                        List<RelativeClassInfo> entities, SharedStorage storage) {
        for (var plugin : plugins) {
            plugin.execute(endpoints, entities, storage);
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            var message = String.format(
                "Plugin '%s' is not found in the classpath", className);

            logger.error(message, e);

            throw new ParserException(message, e);
        }
    }

    private Class<? extends Plugin> processClass(Class<?> cls) {
        if (Plugin.class.isAssignableFrom(cls)) {
            return (Class<? extends Plugin>) cls;
        }

        var message = String.format(
            "Plugin '%s' is not an instance of '%s' interface",
            cls.getName(), Plugin.class.getName());

        logger.error(message);

        throw new ParserException(message);
    }

    private Plugin instantiatePlugin(Class<? extends Plugin> pluginClass) {
        try {
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException e) {
            var message = String.format("Cannot instantiate plugin '%s'",
                pluginClass.getName());

            logger.error(message, e);

            throw new ParserException(message, e);
        }
    }
}
