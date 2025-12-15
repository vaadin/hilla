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
package com.vaadin.hilla.parser.core;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractPlugin<C extends PluginConfiguration>
        implements Plugin {
    private C configuration;

    private SharedStorage storage;

    protected AbstractPlugin() {
    }

    @Override
    public C getConfiguration() {
        return configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(PluginConfiguration configuration) {
        if (configuration == null) {
            this.configuration = null;
            return;
        }

        var configClass = (Class<C>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        if (configClass.equals(PluginConfiguration.class)) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }

        if (!configClass.isAssignableFrom(configuration.getClass())) {
            throw new IllegalArgumentException(
                    String.format("Requires instance of %s " + ", but got %s",
                            configClass, configuration.getClass()));
        }

        this.configuration = (C) configuration;
    }

    protected SharedStorage getStorage() {
        return storage;
    }

    @Override
    public void setStorage(SharedStorage storage) {
        this.storage = storage;
    }
}
