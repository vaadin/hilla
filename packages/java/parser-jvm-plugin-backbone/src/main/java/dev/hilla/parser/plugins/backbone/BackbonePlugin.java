package dev.hilla.parser.plugins.backbone;

import dev.hilla.parser.core.AbstractCompositePlugin;
import dev.hilla.parser.core.PluginConfiguration;

public final class BackbonePlugin
        extends AbstractCompositePlugin<PluginConfiguration> {

    public BackbonePlugin() {
        super(new EndpointPlugin(), new EndpointExposedPlugin(),
                new MethodPlugin(), new MethodParameterPlugin(),
                new EntityPlugin(), new FieldPlugin(),
                new TypeSignaturePlugin(), new JSONPlugin(),
                new FixClassGraphPlugin());
    }
}
