/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.core.basic;

import com.vaadin.hilla.parser.core.AbstractCompositePlugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;

final class BasicPlugin extends AbstractCompositePlugin<PluginConfiguration> {
    private final FinalizePlugin finalizePlugin;

    BasicPlugin() {
        this(new FinalizePlugin());
    }

    private BasicPlugin(FinalizePlugin finalizePlugin) {
        super(new AddPlugin(), new ReplacePlugin(), new RemovePlugin(),
                finalizePlugin);
        this.finalizePlugin = finalizePlugin;
    }

    /**
     * Every step of the walk, one per line, in the order it took them.
     */
    String getFootsteps() {
        return finalizePlugin.getFootsteps();
    }

    /**
     * The named members the walk reached, in the order it reached them.
     */
    String getMembers() {
        return finalizePlugin.getMembers();
    }
}
