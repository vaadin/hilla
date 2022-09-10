package dev.hilla.parser.core.basic;

import dev.hilla.parser.core.AbstractCompositePlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.SharedStorage;

public class BasicPlugin extends AbstractCompositePlugin<PluginConfiguration> {
    public static final String STORAGE_KEY = "BasicPluginResult";
    public static final String FOOTSTEPS_STORAGE_KEY = "BasicPluginFootsteps";
    private int order = 0;

    public BasicPlugin() {
        super(PluginConfiguration.class, new AddPlugin(), new ReplacePlugin()
            , new RemovePlugin(), new FinalizePlugin());
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

}
