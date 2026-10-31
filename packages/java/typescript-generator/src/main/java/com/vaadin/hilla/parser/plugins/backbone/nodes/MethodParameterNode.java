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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;

public class MethodParameterNode extends AbstractNode<Parameter, String>
        implements AnnotatedNode {
    protected MethodParameterNode(@NonNull Parameter source, String target) {
        super(source, target);
    }

    @NonNull
    static public MethodParameterNode of(@NonNull Parameter source,
            @NonNull String target) {
        return new MethodParameterNode(source, target);
    }

    @Override
    public List<Annotation> getAnnotations() {
        return List.of(getSource().getAnnotations());
    }
}
