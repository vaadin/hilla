package dev.hilla.parser.plugins.transfertypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.TypeSignatureNode;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.runtime.transfertypes.EndpointSubscription;
import dev.hilla.runtime.transfertypes.Flux;
import dev.hilla.runtime.transfertypes.Order;
import dev.hilla.runtime.transfertypes.Pageable;
import dev.hilla.runtime.transfertypes.Sort;

public final class TransferTypesPlugin extends AbstractPlugin<PluginConfiguration> {
    static private final Map<String, Class<?>> classMap = new HashMap<>();

    {
        classMap.put("org.springframework.data.domain.Page", List.class);
        classMap.put("org.springframework.data.domain.Pageable",
            Pageable.class);
        classMap.put("org.springframework.data.domain.Sort$Order", Order.class);
        classMap.put("org.springframework.data.domain.Sort", Sort.class);
        classMap.put(UUID.class.getName(), String.class);
        classMap.put("reactor.core.publisher.Flux", Flux.class);
        classMap.put("dev.hilla.EndpointSubscription",
            EndpointSubscription.class);
    }

    public TransferTypesPlugin() {
        super(PluginConfiguration.class);
        setOrder(100);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return NodeDependencies.of(nodeDependencies.getNode(),
            nodeDependencies.getChildNodes().map(this::mapClassNode),
            nodeDependencies.getRelatedNodes().map(this::mapClassNode));
    }

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    private Node<?, ?> mapClassNode(Node<?, ?> node) {
        if (!(node instanceof TypeSignatureNode)) {
            return node;
        }

        var signature = (SignatureModel) node.getSource();
        if (!(signature instanceof ClassRefSignatureModel)) {
            return node;
        }

        var classRef = (ClassRefSignatureModel) signature;
        var className = classRef.getClassInfo().getName();
        if (!classMap.containsKey(className)) {
            return node;
        }

        var mapped = ClassInfoModel.of(classMap.get(className));
        // TODO: replace the setter call with something cleaner
        classRef.setReference(mapped);
        return node;
    }
}
