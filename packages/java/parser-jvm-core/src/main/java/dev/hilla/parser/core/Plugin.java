package dev.hilla.parser.core;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;

public interface Plugin {
    int getOrder();

    void setOrder(int order);

    void execute(List<ClassInfoModel> endpoints);

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }

    default void setParserConfig(ParserConfig config) {}

    void setStorage(@Nonnull SharedStorage storage);
}
