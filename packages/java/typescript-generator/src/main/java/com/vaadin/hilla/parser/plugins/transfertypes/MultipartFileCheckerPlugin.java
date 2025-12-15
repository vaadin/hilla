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

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.FieldInfoModel;
import com.vaadin.hilla.parser.models.MethodInfoModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;

public class MultipartFileCheckerPlugin
        extends AbstractPlugin<PluginConfiguration> {

    private static final String MULTIPART_CLASS_NAME = "org.springframework.web.multipart.MultipartFile";

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode propertyNode) {
            // Get the primary member (field, getter, or setter) to check its
            // type
            var primaryMember = propertyNode.getSource().getPrimaryMember();
            ClassInfoModel nodeType = null;

            if (primaryMember instanceof MethodInfoModel method) {
                // If it's a getter method, get the return type
                if (method.getParameters().isEmpty()) {
                    var resultType = method.getResultType();
                    if (resultType instanceof ClassRefSignatureModel classRef) {
                        nodeType = classRef.getClassInfo();
                    }
                } else if (!method.getParameters().isEmpty()) {
                    // If it's a setter, get the parameter type
                    var paramType = method.getParameters().get(0).getType();
                    if (paramType instanceof ClassRefSignatureModel classRef) {
                        nodeType = classRef.getClassInfo();
                    }
                }
            } else if (primaryMember instanceof FieldInfoModel field) {
                // If it's a field, get the field type
                var fieldType = field.getType();
                if (fieldType instanceof ClassRefSignatureModel classRef) {
                    nodeType = classRef.getClassInfo();
                }
            }

            if (nodeType != null && ClassInfoModel
                    .isAssignableFrom(MULTIPART_CLASS_NAME, nodeType)) {
                throw new MultipartFileUsageException(
                        "MultipartFile is not allowed in entity classes: "
                                + nodeType.getName());
            }
        }

        if (nodePath.getNode() instanceof MethodNode methodNode) {
            var nodeType = methodNode.getSource().getResultType();
            if (nodeType instanceof ClassRefSignatureModel classRefSignatureModel
                    && ClassInfoModel.isAssignableFrom(MULTIPART_CLASS_NAME,
                            classRefSignatureModel.getClassInfo())) {
                throw new MultipartFileUsageException(
                        "MultipartFile is not allowed as return type: "
                                + methodNode.getSource());
            }
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Override
    @NonNull
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

}
