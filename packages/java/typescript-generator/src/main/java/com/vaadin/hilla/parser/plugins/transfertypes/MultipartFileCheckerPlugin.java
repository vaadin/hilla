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
package com.vaadin.hilla.parser.plugins.transfertypes;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.utils.Signatures;

public class MultipartFileCheckerPlugin
        extends AbstractPlugin<PluginConfiguration> {

    private static final String MULTIPART_CLASS_NAME = "org.springframework.web.multipart.MultipartFile";

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode propertyNode) {
            // Get the primary member (field, getter, or setter) to check its
            // type
            var primaryMember = propertyNode.getPrimaryMember();
            AnnotatedType memberType = null;

            if (primaryMember instanceof Method method) {
                // A getter returns the value, a setter takes it
                memberType = method.getParameterCount() == 0
                        ? method.getAnnotatedReturnType()
                        : method.getParameters()[0].getAnnotatedType();
            } else if (primaryMember instanceof Field field) {
                memberType = field.getAnnotatedType();
            }

            var nodeType = Optional.ofNullable(memberType)
                    .filter(Signatures::isClassRef)
                    .flatMap(Signatures::getClass).orElse(null);

            if (nodeType != null && Signatures
                    .isAssignableFrom(MULTIPART_CLASS_NAME, nodeType)) {
                throw new MultipartFileUsageException(
                        "MultipartFile is not allowed in entity classes: "
                                + nodeType.getName());
            }
        }

        if (nodePath.getNode() instanceof MethodNode methodNode) {
            var nodeType = methodNode.getSource().getAnnotatedReturnType();
            if (Signatures.isClassRef(nodeType)
                    && Signatures.isAssignableFrom(MULTIPART_CLASS_NAME,
                            Signatures.getClass(nodeType).orElseThrow())) {
                throw new MultipartFileUsageException(
                        "MultipartFile is not allowed as return type: "
                                + methodNode.getSource());
            }
        }
    }

    @Override
    @NonNull
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

}
