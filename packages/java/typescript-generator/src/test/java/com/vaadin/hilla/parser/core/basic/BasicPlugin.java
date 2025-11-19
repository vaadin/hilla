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
    public static final String FOOTSTEPS_STORAGE_KEY = "x-basic-plugin-footsteps";
    public static final String STORAGE_KEY = "x-basic-plugin-result";
    private int order = 0;

    BasicPlugin() {
        super(new AddPlugin(), new ReplacePlugin(), new RemovePlugin(),
                new FinalizePlugin());
    }
}
