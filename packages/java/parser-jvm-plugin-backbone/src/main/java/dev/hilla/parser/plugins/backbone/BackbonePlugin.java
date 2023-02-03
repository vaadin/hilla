package dev.hilla.parser.plugins.backbone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import dev.hilla.parser.core.AbstractCompositePlugin;
import dev.hilla.parser.core.PluginConfiguration;

public final class BackbonePlugin
        extends AbstractCompositePlugin<PluginConfiguration> {
    public BackbonePlugin() {
        super(new EndpointPlugin(), new EndpointExposedPlugin(),
                new MethodPlugin(), new MethodParameterPlugin(),
                new EntityPlugin(),
                new PropertyPlugin(new ObjectMapper()
                        .registerModule(new ParameterNamesModule())
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule())),
                new TypeSignaturePlugin());
    }
}
