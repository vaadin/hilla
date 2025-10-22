package com.vaadin.hilla.typescript.parser.core.basic;

import com.vaadin.hilla.typescript.parser.core.AbstractCompositePlugin;
import com.vaadin.hilla.typescript.parser.core.PluginConfiguration;

final class BasicPlugin extends AbstractCompositePlugin<PluginConfiguration> {
    public static final String FOOTSTEPS_STORAGE_KEY = "x-basic-plugin-footsteps";
    public static final String STORAGE_KEY = "x-basic-plugin-result";
    private int order = 0;

    BasicPlugin() {
        super(new AddPlugin(), new ReplacePlugin(), new RemovePlugin(),
                new FinalizePlugin());
    }
}
