package com.vaadin.fusion.parser.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public abstract class ConfigurationList<Item> {
    private final Set<Item> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<Item> use = new HashSet<>();

    public ConfigurationList() {
    }

    public ConfigurationList(@Nonnull Collection<Item> use,
            @Nonnull Collection<Item> disable) {
        this.disable.addAll(disable);
        this.use.addAll(use);
    }

    public Set<Item> getDisable() {
        return disable;
    }

    public Set<Item> getUse() {
        return use;
    }

    public boolean isDisableAllDefaults() {
        return disableAllDefaults;
    }

    public abstract static class Processor<Item> {
        private final ConfigurationList<Item> config;
        private final Set<Item> defaults;

        public Processor(ConfigurationList<Item> config, Set<Item> defaults) {
            this.config = config;
            this.defaults = defaults;
        }

        public ConfigurationList<Item> getConfig() {
            return config;
        }

        public Set<Item> getDefaults() {
            return defaults;
        }

        public Collection<Item> process() {
            var stream = Objects.requireNonNull(config).getUse().stream();

            if (!config.isDisableAllDefaults()) {
                stream = Stream.concat(defaults.stream().filter(
                        plugin -> !config.getDisable().contains(plugin)),
                        stream);
            }

            return Collections.unmodifiableCollection(stream.collect(
                    Collectors.toMap(Object::hashCode, Function.identity()))
                    .values());
        }
    }
}
