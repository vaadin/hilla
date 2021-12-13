package com.vaadin.fusion.parser.core;

import java.lang.reflect.InvocationTargetException;

public final class PluginLoader {
    private static final ClassLoader loader = PluginLoader.class
            .getClassLoader();

    private final String name;
    private final PluginConfiguration config;

    public PluginLoader(String name, PluginConfiguration config) {
        this.name = name;
        this.config = config;
    }

    public Plugin load() {
        var cls = processClass(loadClass());
        var instance = instantiatePlugin(cls);
        instance.setConfig(config);
        return instance;
    }

    private Plugin instantiatePlugin(Class<? extends Plugin> pluginClass) {
        try {
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new ParserException(
                    String.format("Cannot instantiate plugin '%s'", name), e);
        }
    }

    private Class<?> loadClass() {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ParserException(String.format(
                    "Plugin '%s' is not found in the classpath", name), e);
        }
    }

    private Class<? extends Plugin> processClass(Class<?> cls) {
        if (Plugin.class.isAssignableFrom(cls)) {
            return (Class<? extends Plugin>) cls;
        }

        throw new ParserException(String.format(
                "Plugin '%s' is not an instance of '%s' interface", name,
                Plugin.class.getName()));
    }
}
