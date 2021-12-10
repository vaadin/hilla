package com.vaadin.fusion.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;
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
        plugins = config.getPlugins().stream().map(this::loadClass)
                .map(this::processClass).map(this::instantiatePlugin)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void execute(Collection<RelativeClassInfo> endpoints,
            Collection<RelativeClassInfo> entities, SharedStorage storage) {
        for (var plugin : plugins) {
            logger.debug("Executing plugin " + plugin.getClass().getName());
            plugin.execute(endpoints, entities, storage);
        }
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
}
