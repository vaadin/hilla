package dev.hilla.parser.core.basic;

import dev.hilla.parser.core.AbstractCompositePlugin;
import dev.hilla.parser.core.PluginConfiguration;

final class BasicPlugin extends AbstractCompositePlugin<PluginConfiguration> {
    public static final String STORAGE_KEY = "x-basic-plugin-result";
    public static final String FOOTSTEPS_STORAGE_KEY = "x-basic-plugin-footsteps";
    private int order = 0;

    BasicPlugin() {
        super(new AddPlugin(), new ReplacePlugin(), new RemovePlugin(),
                new FinalizePlugin());
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
