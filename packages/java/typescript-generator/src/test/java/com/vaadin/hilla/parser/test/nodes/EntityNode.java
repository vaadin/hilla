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
package com.vaadin.hilla.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class EntityNode extends AbstractNode<ClassInfoModel, Schema<?>> {
    private EntityNode(@NonNull ClassInfoModel source,
            @NonNull ObjectSchema target) {
        super(source, target);
    }

    @NonNull
    static public EntityNode of(@NonNull ClassInfoModel model) {
        return new EntityNode(model, new ObjectSchema());
    }
}
