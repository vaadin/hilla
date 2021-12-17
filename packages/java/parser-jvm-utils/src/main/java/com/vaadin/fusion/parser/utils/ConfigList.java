package com.vaadin.fusion.parser.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ConfigList<Item> {
    Collection<Item> getDisable();

    Collection<Item> getUse();

    boolean isDisableAllDefaults();

    abstract class Processor<Item> {
        private final ConfigList<Item> config;
        private final Set<Item> defaults;

        public Processor(ConfigList<Item> config, Set<Item> defaults) {
            this.config = config;
            this.defaults = defaults;
        }

        public ConfigList<Item> getConfig() {
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

            return Collections.unmodifiableCollection(stream
                    .collect(Collectors.toMap(Object::hashCode,
                            Function.identity(), (oldValue, value) -> value))
                    .values());
        }
    }
}
