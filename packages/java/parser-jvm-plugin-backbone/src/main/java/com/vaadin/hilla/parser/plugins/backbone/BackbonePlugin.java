package com.vaadin.hilla.parser.plugins.backbone;

import com.vaadin.hilla.parser.core.AbstractCompositePlugin;

public final class BackbonePlugin
        extends AbstractCompositePlugin<BackbonePluginConfiguration> {
    public BackbonePlugin() {
        super(new EndpointPlugin(), new EndpointExposedPlugin(),
                new MethodPlugin(), new MethodParameterPlugin(),
                new EntityPlugin(), new PropertyPlugin(),
                new TypeSignaturePlugin());
    }
}
