package dev.hilla.parser.core;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractPlugin<C extends PluginConfiguration>
        implements Plugin {
    private C configuration;
    private int order;

    private SharedStorage storage;

    protected AbstractPlugin() {
    }

    @Override
    public C getConfiguration() {
        return configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(PluginConfiguration configuration) {
        if (configuration == null) {
            this.configuration = null;
            return;
        }

        var configClass = (Class<C>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        if (configClass.equals(PluginConfiguration.class)) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }

        if (!configClass.isAssignableFrom(configuration.getClass())) {
            throw new IllegalArgumentException(
                    String.format("Requires instance of %s " + ", but got %s",
                            configClass, configuration.getClass()));
        }

        this.configuration = (C) configuration;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    protected SharedStorage getStorage() {
        return storage;
    }

    @Override
    public void setStorage(SharedStorage storage) {
        this.storage = storage;
    }
}
