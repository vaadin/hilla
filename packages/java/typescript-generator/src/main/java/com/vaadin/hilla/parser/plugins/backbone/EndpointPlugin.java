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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode;
import com.vaadin.hilla.parser.utils.Signatures;

public final class EndpointPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<Class<?>> endpoints) {
        endpoints.stream().map(MethodPlugin::getMethods)
                .flatMap(Collection::stream).map(Method::getParameters)
                .flatMap(Arrays::stream).findFirst().ifPresent((parameter) -> {
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
            endpointNode.setTarget(getEndpointName(endpointNode.getSource()));
        }
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodeDependencies.getNode();
            var endpoints = rootNode.getSource();
            checkIfJavaCompilerParametersFlagIsEnabled(endpoints);
            return nodeDependencies.appendChildNodes(endpoints.stream()
                    .filter(Predicate.not(Signatures::isJDKClass))
                    .map(EndpointNode::of));
        }
        return nodeDependencies;
    }

    private String getEndpointName(Class<?> endpointCls) {
        var endpointAnnotations = getParserConfig().getEndpointAnnotations();
        var endpointAnnotation = Arrays.stream(endpointCls.getAnnotations())
                .filter(annotation -> endpointAnnotations
                        .contains(annotation.annotationType()))
                .findFirst();
        return endpointAnnotation.flatMap(this::getEndpointAnnotationValue)
                .filter(name -> !name.isEmpty())
                .orElseGet(endpointCls::getSimpleName);
    }

    private Optional<String> getEndpointAnnotationValue(
            Annotation endpointAnnotation) {
        try {
            var value = endpointAnnotation.annotationType().getMethod("value");
            return Optional.of((String) value.invoke(endpointAnnotation));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BackbonePluginException(
                    "Unable to read the name of the endpoint", e);
        }
    }
}
