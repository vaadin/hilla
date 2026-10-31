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
package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.SerializationConfig;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.parser.core.AbstractNode;

public class EntityNode extends AbstractNode<Class<?>, EntityModel.Builder> {
    private SerializationConfig serializationConfig;

    protected EntityNode(@NonNull Class<?> source,
            EntityModel.@NonNull Builder target) {
        super(source, target);
    }

    /**
     * The Jackson configuration the properties of the entity are read with, the
     * application's own when it configures an object mapper factory, so that a
     * plugin asking about a property sees the same properties, under the same
     * names, as the ones generated.
     */
    public SerializationConfig getSerializationConfig() {
        return serializationConfig;
    }

    public void setSerializationConfig(
            SerializationConfig serializationConfig) {
        this.serializationConfig = serializationConfig;
    }

    @NonNull
    static public EntityNode of(@NonNull Class<?> cls) {
        return new EntityNode(cls, new EntityModel.Builder());
    }
}
