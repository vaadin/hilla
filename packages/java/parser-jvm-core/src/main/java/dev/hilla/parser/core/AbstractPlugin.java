package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import java.util.Objects;

public abstract class AbstractPlugin<C extends PluginConfiguration> implements Plugin {
    private final Class<? extends C> configurationClass;
    private C configuration;
    private int order;

    private SharedStorage storage;

    protected AbstractPlugin(
        @Nonnull Class<? extends C> configurationClass) {
        this.configurationClass = Objects.requireNonNull(configurationClass);
    }

    @SuppressWarnings("unchecked")
    protected AbstractPlugin() {
        this((Class<? extends C>) PluginConfiguration.class);
    }

    @Override
    @Nonnull
    public C getConfiguration() {
        return configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(@Nonnull PluginConfiguration configuration) {
        Objects.requireNonNull(configuration);
        if (!configurationClass.isAssignableFrom(configuration.getClass())) {
            throw new IllegalArgumentException(
                String.format("Instance of %s " + " required, but got %s",
                    configurationClass, configuration.getClass()));
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
