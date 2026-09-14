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

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel;
import com.vaadin.hilla.parser.plugins.backbone.TypeFacts;

public class PropertyNode extends AbstractNode<JacksonPropertyModel, String> {
    private TypeFacts valueType = TypeFacts.unknown();

    protected PropertyNode(@NonNull JacksonPropertyModel source,
            @NonNull String target) {
        super(source, target);
    }

    /**
     * What the walk says about the type of the value the property holds, which
     * the walk carries as a node below this one. Until that node is left, it is
     * what is known about a type nothing has been walked for.
     */
    public TypeFacts getValueType() {
        return valueType;
    }

    public void setValueType(TypeFacts valueType) {
        this.valueType = valueType;
    }

    @NonNull
    static public PropertyNode of(@NonNull JacksonPropertyModel source) {
        return new PropertyNode(source, "");
    }
}
