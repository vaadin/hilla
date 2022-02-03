package dev.hilla.parser.core;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public interface Plugin {
    int getOrder();

    void setOrder(int order);

    void setStorage(@Nonnull SharedStorage storage);

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }

    interface Transformer extends Plugin {
        @Nonnull
        Stream<RelativeClassInfo> transform(
                @Nonnull Stream<RelativeClassInfo> stream);
    }

    interface Processor extends Plugin {
        void process(@Nonnull Collection<RelativeClassInfo> endpoints,
                @Nonnull Collection<RelativeClassInfo> entities);
    }
}
