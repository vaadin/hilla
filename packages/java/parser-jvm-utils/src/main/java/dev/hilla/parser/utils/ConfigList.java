package dev.hilla.parser.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic structure for configuring a plugin. It assumes that the plugin have
 * some list of default options that could be disabled by the user. Also, the
 * user is able to add their own options.
 *
 * The implementation class is assumed to be used with the reflection approach
 * of tools like Jackson or Maven.
 *
 * For Maven configuration, it could look like the following:
 *
 * {@code
 * <configuration>
 *   <annotations>
 *       <disableAllDefaults>false</disableAllDefaults>
 *       <disable>
 *           <annotation>javax.annotations.Nonnull</annotation>
 *       </disable>
 *       <use>
 *           <annotation>io.github.my.annotations.Nonnull</annotation>
 *       </use>
 *   </annotations>
 * </configuration>
 * }
 *
 * @param <Item>
 *            a type of option
 */
public interface ConfigList<Item> {
    /**
     * Gets a list of default options the user wants to disable.
     *
     * @return a list of disabled options.
     */
    Collection<Item> getDisabledOptions();

    /**
     * Gets a list of custom options the user wants to use.
     *
     * @return a list of used options.
     */
    Collection<Item> getUsedOptions();

    /**
     * Indicates if the user wants to disable all default options.
     *
     * @return a flag of disabling all the default options.
     */
    boolean shouldAllDefaultsBeDisabled();

    /**
     * Analyzer for the ConfigList class. It accepts the ConfigList and the
     * defaults and constructs the result collection.
     *
     * @param <Item>
     *            a type of option
     */
    abstract class Processor<Item> {
        private final Collection<Item> defaults;
        private ConfigList<Item> config;

        public Processor(Collection<Item> defaults) {
            this(null, defaults);
        }

        public Processor(ConfigList<Item> config, Collection<Item> defaults) {
            this.config = config;
            this.defaults = defaults;
        }

        public ConfigList<Item> getConfig() {
            return config;
        }

        public void setConfig(ConfigList<Item> config) {
            this.config = config;
        }

        public Collection<Item> getDefaults() {
            return defaults;
        }

        public Collection<Item> process() {
            if (config == null) {
                return defaults;
            }

            var stream = Objects.requireNonNull(config).getUsedOptions()
                    .stream();

            if (!config.shouldAllDefaultsBeDisabled()) {
                stream = Stream.concat(
                        defaults.stream().filter(plugin -> !config
                                .getDisabledOptions().contains(plugin)),
                        stream);
            }

            return Collections.unmodifiableCollection(stream
                    .collect(Collectors.toMap(Object::hashCode,
                            Function.identity(), (oldValue, value) -> value))
                    .values());
        }
    }
}
