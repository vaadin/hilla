package dev.hilla.parser.core;

import java.lang.reflect.InvocationTargetException;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginManager {
    private static final ClassLoader loader = PluginManager.class
            .getClassLoader();
    private static final Logger logger = LoggerFactory
            .getLogger(PluginManager.class);
    private final ChangeListener<Integer> listener;
    private final SortedSet<Plugin> plugins;

    PluginManager(ParserConfig config, SharedStorage storage) {
        plugins = config.getPlugins();
        listener = new ChangeListener<>(
                () -> storage.getClassMappers().hashCode());

        for (var plugin : plugins) {
            plugin.setStorage(storage);
        }
    }

    public static Plugin load(String name, Integer order,
            PluginConfiguration config) {
        var cls = processClass(loadClass(name));
        var instance = instantiatePlugin(cls);
        instance.setConfig(config);

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

    private static Class<? extends Plugin> processClass(Class<?> cls) {
        if (Plugin.class.isAssignableFrom(cls)) {
            return (Class<? extends Plugin>) cls;
        }

        throw new ParserException(String.format(
                "Plugin '%s' is not an instance of '%s' interface",
                cls.getName(), Plugin.class.getName()));
    }

    public void preprocess() {
        for (var plugin : plugins) {
            if (plugin instanceof Plugin.Preprocessor) {
                logger.debug("Executing preprocessor plugin "
                        + plugin.getClass().getName());

                ((Plugin.Preprocessor) plugin).preprocess();
            }
        }
    }

    public void process(ScanElementsCollector collector) {
        listener.onChange(collector::collect);

        for (var plugin : plugins) {
            if (plugin instanceof Plugin.Processor) {
                logger.debug("Executing processor plugin "
                        + plugin.getClass().getName());

                ((Plugin.Processor) plugin).process(collector.getEndpoints(),
                        collector.getEntities());

                listener.poll();
            }
        }
    }
}
