package com.vaadin.fusion.parser.core;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PluginManager {
    private static final Logger logger = LoggerFactory
            .getLogger(PluginManager.class);

    private final Set<Plugin> plugins;

    PluginManager(ParserConfig config) {
        plugins = config.getPlugins();
    }

    public void execute(Collection<RelativeClassInfo> endpoints,
            Collection<RelativeClassInfo> entities, SharedStorage storage) {
        for (var plugin : plugins) {
            logger.debug("Executing plugin " + plugin.getClass().getName());
            plugin.execute(endpoints, entities, storage);
        }
    }
}
