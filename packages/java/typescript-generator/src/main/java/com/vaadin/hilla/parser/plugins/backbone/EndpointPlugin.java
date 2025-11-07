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
package com.vaadin.hilla.parser.plugins.backbone;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

import io.swagger.v3.oas.models.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.MethodInfoModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;

public final class EndpointPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<ClassInfoModel> endpoints) {
        endpoints.stream().map(ClassInfoModel::getMethods)
                .flatMap(Collection::stream).map(MethodInfoModel::getParameters)
                .flatMap(Collection::stream).findFirst()
                .ifPresent((parameter) -> {
                    if (parameter.getName() == null) {
                        logger.info("Missing endpoint method parameter names"
                                + " in JVM bytecode, probably because they were not enabled"
                                + " during compilation. For the Java compiler, set the"
                                + " \"parameters\" flag to true to enable them.");
                    }
                });
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EndpointNode) {
            var endpointNode = (EndpointNode) nodePath.getNode();
            var name = getEndpointName(endpointNode.getSource());
            endpointNode.setTarget(new Tag().name(name));
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof EndpointNode && parentNode instanceof RootNode) {
            ((RootNode) parentNode).getTarget()
                    .addTagsItem(((EndpointNode) node).getTarget());
        }
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodeDependencies.getNode();
            var endpoints = rootNode.getSource().stream()
                    .map(ClassInfoModel::of).toList();
            checkIfJavaCompilerParametersFlagIsEnabled(endpoints);
            return nodeDependencies.appendChildNodes(
                    endpoints.stream().filter(ClassInfoModel::isNonJDKClass)
                            .map(EndpointNode::of));
        }
        return nodeDependencies;
    }

    private String getEndpointName(ClassInfoModel endpointCls) {
        var endpointAnnotations = getStorage().getParserConfig()
                .getEndpointAnnotations();
        var endpointAnnotation = endpointCls.getAnnotations().stream()
                .filter(annotation -> endpointAnnotations.contains(
                        ((Annotation) annotation.get()).annotationType()))
                .findFirst();
        return endpointAnnotation.flatMap(this::getEndpointAnnotationValue)
                .filter(name -> !name.isEmpty())
                .orElseGet(endpointCls::getSimpleName);
    }

    private Optional<String> getEndpointAnnotationValue(
            AnnotationInfoModel endpointAnnotation) {
        return endpointAnnotation.getParameters().stream()
                .filter(param -> param.getName().equals("value")).findFirst()
                .map(param -> (String) param.getValue());
    }
}
