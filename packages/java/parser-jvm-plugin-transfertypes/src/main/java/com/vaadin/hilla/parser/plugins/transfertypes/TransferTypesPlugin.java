package com.vaadin.hilla.parser.plugins.transfertypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.NonNull;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;

import static java.util.Map.entry;

public final class TransferTypesPlugin
    extends AbstractPlugin<PluginConfiguration> {
    private static final Map<String, TransferType> classMap = Map.ofEntries(
        entry("org.springframework.data.domain.Page", Import.NAMED(
            "@vaadin/hilla-frontend", "Page")),
        entry("org.springframework.data.domain.Pageable", Import.NAMED("@vaadin/hilla-frontend", "Pageable")),
        entry("org.springframework.data.domain.Sort$Order", Import.NAMED("@vaadin/hilla-frontend", "Order")),
        entry("org.springframework.data.domain.Sort", Import.NAMED("@vaadin/hilla-frontend", "Sort")),
        entry(UUID.class.getName(), TypeScriptStandardLibrary.STRING),
        entry("reactor.core.publisher.Flux", Import.NAMED("@vaadin/hilla-frontend", "Flux")),
        entry("com.vaadin.hilla.EndpointSubscription",
            Import.NAMED("@vaadin/hilla-frontend", "EndpointSubscription")),
        entry(JsonNode.class.getName(), TypeScriptStandardLibrary.OBJECT),
        entry(ObjectNode.class.getName(), TypeScriptStandardLibrary.OBJECT),
        entry(ArrayNode.class.getName(), TypeScriptStandardLibrary.ARRAY),
        entry("org.springframework.web.multipart.MultipartFile",
            TypeScriptStandardLibrary.TYPE("File")),
        entry("com.vaadin.hilla.signals.Signal", Import.NAMED("@vaadin/hilla-react-signals", "Signal")),
        entry("com.vaadin.hilla.signals.ValueSignal", Import.NAMED("@vaadin/hilla-react-signals", "ValueSignal")),
        entry("com.vaadin.hilla.signals.NumberSignal",
            Import.NAMED("@vaadin/hilla-react-signals", "NumberSignal")),
        entry("com.vaadin.hilla.signals.ListSignal", Import.NAMED("@vaadin/hilla-react-signals", "ListSignal"))
    );


    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypedNode typedNode)) {
            return;
        }

        if (typedNode.getType() instanceof ClassRefSignatureModel classRef && classMap.containsKey(
            classRef.getName())) {
            var transferType = classMap.get(classRef.getName());
            var schema = new Schema<>().type("object");
            var extensions = new HashMap<String, Object>();

            if (transferType instanceof Import type) {
                var fromModule = new HashMap<String, String>();
                fromModule.put("module", type.module());
                fromModule.put(type.type(), type.specifier());
                extensions.put("x-from-module", fromModule);
            } else if (transferType instanceof TypeScriptStandardLibrary.Primitive primitive) {
                schema.setType(primitive.type());
            } else if (transferType instanceof TypeScriptStandardLibrary.Type type) {
                extensions.put("x-std-type", type.type());
            } else {
                throw new IllegalStateException("Unexpected value: " + transferType);
            }

            typedNode.getTarget().setAnyOf(List.of(schema.extensions(extensions)));
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();

        if (!(node instanceof TypedNode typedNode)) {
            return nodeDependencies;
        }

        if (typedNode.getType() instanceof ClassRefSignatureModel classRef && classMap.containsKey(
            classRef.getName())) {
            return nodeDependencies.processRelatedNodes((nodes) -> nodes.filter((n) -> !(n instanceof EntityNode)));
        }

        return nodeDependencies;
    }
}
