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
import com.vaadin.hilla.parser.models.SignatureModel;

public final class EndpointSignatureNode
        extends AbstractNode<SignatureModel, Void> {
    private EndpointSignatureNode(@NonNull SignatureModel source) {
        super(source, null);
    }

    @NonNull
    static public EndpointSignatureNode of(@NonNull SignatureModel source) {
        return new EndpointSignatureNode(source);
    }
}
