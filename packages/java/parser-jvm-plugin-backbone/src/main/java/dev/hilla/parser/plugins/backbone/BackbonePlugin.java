package dev.hilla.parser.plugins.backbone;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.parser.core.AbstractCompositePlugin;
import dev.hilla.parser.core.PluginConfiguration;

public final class BackbonePlugin
        extends AbstractCompositePlugin<PluginConfiguration> {

    public BackbonePlugin() {
        super(new EndpointPlugin(), new EndpointExposedPlugin(),
                new MethodPlugin(), new MethodParameterPlugin(),
                new EntityPlugin(), new PropertyPlugin(new ObjectMapper()),
                new TypeSignaturePlugin());
    }
}
