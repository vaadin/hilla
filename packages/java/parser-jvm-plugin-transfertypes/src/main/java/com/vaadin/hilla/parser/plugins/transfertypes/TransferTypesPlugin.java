package com.vaadin.hilla.parser.plugins.transfertypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.mappedtypes.Order;
import com.vaadin.hilla.mappedtypes.Pageable;
import com.vaadin.hilla.mappedtypes.Sort;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.runtime.transfertypes.EndpointSubscription;
import com.vaadin.hilla.runtime.transfertypes.File;
import com.vaadin.hilla.runtime.transfertypes.Flux;
import com.vaadin.hilla.runtime.transfertypes.ListSignal;
import com.vaadin.hilla.runtime.transfertypes.NumberSignal;
import com.vaadin.hilla.runtime.transfertypes.Signal;
import com.vaadin.hilla.runtime.transfertypes.ValueSignal;
import com.vaadin.hilla.transfertypes.annotations.FromModule;

public final class TransferTypesPlugin
        extends AbstractPlugin<PluginConfiguration> {
    private static final Map<String, Class<?>> classMap = new HashMap<>();

    static {
        classMap.put("org.springframework.data.domain.Page", List.class);
        classMap.put("org.springframework.data.domain.Pageable",
                Pageable.class);
        classMap.put("org.springframework.data.domain.Sort$Order", Order.class);
        classMap.put("org.springframework.data.domain.Sort", Sort.class);
        classMap.put(UUID.class.getName(), String.class);
        classMap.put("reactor.core.publisher.Flux", Flux.class);
        classMap.put("com.vaadin.hilla.EndpointSubscription",
                EndpointSubscription.class);
        classMap.put(JsonNode.class.getName(), Object.class);
        classMap.put(ObjectNode.class.getName(), Object.class);
        classMap.put(ArrayNode.class.getName(), List.class);
        classMap.put("org.springframework.web.multipart.MultipartFile",
                File.class);
        classMap.put("com.vaadin.hilla.signals.Signal", Signal.class);
        classMap.put("com.vaadin.hilla.signals.ValueSignal", ValueSignal.class);
        classMap.put("com.vaadin.hilla.signals.NumberSignal",
                NumberSignal.class);
        classMap.put("com.vaadin.hilla.signals.ListSignal", ListSignal.class);
    }

    private final Set<String> replacedClasses = new HashSet<>();

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EntityNode entityNode && nodePath
                .getParentPath().getNode() instanceof RootNode rootNode) {
            var cls = entityNode.getSource();
            var name = cls.getName();
            if (replacedClasses.contains(name)) {
                var schema = entityNode.getTarget();

                cls.getAnnotations().stream()
                        .filter((model) -> model.getName()
                                .equals(FromModule.class.getName()))
                        .findFirst().ifPresent((annotationModel) -> {
                            var annotation = (FromModule) annotationModel.get();
                            var namedSpecifier = annotation.namedSpecifier();
                            var defaultSpecifier = annotation
                                    .defaultSpecifier();

                            if (namedSpecifier.isBlank()
                                    && defaultSpecifier.isBlank()) {
                                throw new IllegalArgumentException(String
                                        .format("@FromImport annotation for class %s must have at least one named specifier or a default specifier",
                                                name));
                            }

                            var fromModule = new HashMap<String, Object>();
                            fromModule.put("module", annotation.module());

                            if (!namedSpecifier.isBlank()) {
                                fromModule.put("named", namedSpecifier);
                            }

                            if (!defaultSpecifier.isBlank()) {
                                fromModule.put("default", defaultSpecifier);
                            }

                            schema.addExtension("x-from-module", fromModule);
                        });
            }
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> nodePath) {
        if (!(node instanceof TypedNode typedNode)) {
            return node;
        }

        return (Node<?, ?>) typedNode.processType((signature) -> {
            if (!(signature instanceof ClassRefSignatureModel classRef)) {
                return signature;
            }

            var className = classRef.getClassInfo().getName();
            if (!classMap.containsKey(className)) {
                return signature;
            }

            var mappedClassInfo = ClassInfoModel.of(classMap.get(className));

            // Adding the class name to the shared data set to be able to add
            // the import metadata if present.
            replacedClasses.add(mappedClassInfo.getName());

            return ClassRefSignatureModel.of(mappedClassInfo,
                    classRef.getTypeArguments(), classRef.getAnnotations());
        });
    }
}
