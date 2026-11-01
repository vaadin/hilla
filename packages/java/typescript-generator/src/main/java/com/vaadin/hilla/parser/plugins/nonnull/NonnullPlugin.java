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
package com.vaadin.hilla.parser.plugins.nonnull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.generator.typescript.EntityDeclaration;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Output;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.AnnotatedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Signatures;

public final class NonnullPlugin extends AbstractPlugin<NonnullPluginConfig> {
    /**
     * Annotations which can be placed on other elements too, but are only
     * supported on packages, as the endpoint runtime only checks them there.
     */
    private static final Set<String> PACKAGE_ONLY_ANNOTATIONS = Set
            .of("org.jspecify.annotations.NullMarked");

    private Map<String, AnnotationMatcher> annotationsMap = mapByName(
            NonnullPluginConfig.Processor.defaults);

    public NonnullPlugin() {
        super();
    }

    static private Map<String, AnnotationMatcher> mapByName(
            Collection<AnnotationMatcher> annotations) {
        return annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();

        if (node.getTarget() instanceof TypeModel.Builder type) {
            var nodeSource = node.getSource();

            if (nodeSource instanceof AnnotatedElement signature
                    && Signatures.isOptional(signature)) {
                // Optional is always nullable, regardless of annotations
                type.setOptional(true);
            } else {
                Stream<Annotation> annotations = Stream.empty();

                // Apply from current node, if it is annotated
                if (node instanceof AnnotatedNode) {
                    annotations = ((AnnotatedNode) node).getAnnotations()
                            .stream();
                }

                annotations = considerAscendantAnnotations(annotations,
                        nodePath);

                // Apply annotations from package (NonNullApi, NullMarked)
                annotations = Stream.concat(
                        getPackageAnnotationsStream(nodePath),
                        annotations
                                .filter(annotation -> !PACKAGE_ONLY_ANNOTATIONS
                                        .contains(annotation.annotationType()
                                                .getName())));

                computeNullabilityFromAnnotations(annotations)
                        .ifPresent(type::setOptional);

            }
        }
    }

    /**
     * Marks the properties of a declaration which can be absent, which is how
     * the nullability settled on the way out of the nodes reaches the file the
     * entity is declared in.
     */
    @Override
    public void contribute(NodePath<?> nodePath, Output output) {
        if (nodePath.getNode() instanceof EntityNode entity) {
            output.of(entity, EntityDeclaration.class)
                    .forEach(NonnullPlugin::markOptionalProperties);
        }
    }

    private static void markOptionalProperties(EntityDeclaration declaration) {
        if (declaration.entity() instanceof EntityModel.Bean bean) {
            bean.properties().stream()
                    .filter(property -> property.type().optional())
                    .forEach(property -> declaration
                            .markOptional(property.name()));
        }
    }

    private Optional<Boolean> computeNullabilityFromAnnotations(
            Stream<Annotation> annotations) {
        return annotations
                .map(annotation -> annotationsMap
                        .get(annotation.annotationType().getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .map(AnnotationMatcher::doesMakeNullable);
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @Override
    public void setConfiguration(@NonNull PluginConfiguration configuration) {
        super.setConfiguration(configuration);
        this.annotationsMap = mapByName(
                new NonnullPluginConfig.Processor(getConfiguration())
                        .process());
    }

    private Optional<Package> findClosestPackage(NodePath<?> nodePath) {
        return nodePath.stream().map(NodePath::getNode)
                .filter(node -> node.getSource() instanceof Class<?>)
                .map(node -> (Class<?>) node.getSource()).findFirst()
                .map(Class::getPackage);
    }

    private Stream<Annotation> getPackageAnnotationsStream(
            NodePath<?> nodePath) {
        return findClosestPackage(nodePath).stream()
                .map(Package::getDeclaredAnnotations).flatMap(Arrays::stream);
    }

    /**
     * Adds ascendant annotations for check in case the type is annotated on
     * method/parameter/property level.
     *
     * @param annotations
     *            initial type annotations
     * @param nodePath
     *            the node path
     * @return stream of all annotations to check
     */
    private Stream<Annotation> considerAscendantAnnotations(
            Stream<Annotation> annotations, NodePath<?> nodePath) {
        var current = nodePath.getNode();
        var parent = nodePath.getParentPath().getNode();

        if (current instanceof TypedNode) {
            if (parent instanceof MethodNode
                    || parent instanceof MethodParameterNode
                    || parent instanceof PropertyNode) {
                annotations = Stream.concat(annotations,
                        ((AnnotatedNode) parent).getAnnotations().stream());
            }
        }

        return annotations;
    }
}
