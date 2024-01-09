package com.vaadin.hilla.parser.plugins.backbone;

import com.vaadin.hilla.parser.core.PluginConfiguration;

public class BackbonePluginConfiguration implements PluginConfiguration {
    private String objectMapperFactoryClassName;

    public String getObjectMapperFactoryClassName() {
        return objectMapperFactoryClassName;
    }

    public void setObjectMapperFactoryClassName(
            String objectMapperFactoryClassName) {
        this.objectMapperFactoryClassName = objectMapperFactoryClassName;
    }
}
