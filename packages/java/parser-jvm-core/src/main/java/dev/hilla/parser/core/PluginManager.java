package dev.hilla.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public final class PluginManager
        extends AbstractCompositePlugin<PluginConfiguration> {
    private static final ClassLoader loader = PluginManager.class
            .getClassLoader();

    PluginManager(Collection<Plugin> plugins) {
        super(plugins.toArray(Plugin[]::new));
    }

    public static Plugin load(String name, Integer order,
            PluginConfiguration configuration) {
        var cls = processClass(loadClass(name));
        var instance = instantiatePlugin(cls);
        instance.setConfiguration(configuration);

        if (order != null) {
            instance.setOrder(order);
        }

        return instance;
    }

    private static Plugin instantiatePlugin(
            Class<? extends Plugin> pluginClass) {
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

    private static Class<?> loadClass(String name) {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ParserException(String.format(
                    "Plugin '%s' is not found in the classpath", name), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Plugin> processClass(Class<?> cls) {
        if (Plugin.class.isAssignableFrom(cls)) {
            return (Class<? extends Plugin>) cls;
        }

        throw new ParserException(String.format(
                "Plugin '%s' is not an instance of '%s' interface",
                cls.getName(), Plugin.class.getName()));
    }
}
