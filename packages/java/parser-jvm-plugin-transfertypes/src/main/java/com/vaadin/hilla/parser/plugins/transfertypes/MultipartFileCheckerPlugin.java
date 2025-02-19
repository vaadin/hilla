package com.vaadin.hilla.parser.plugins.transfertypes;

import java.util.Objects;

import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;

public class MultipartFileCheckerPlugin
        extends AbstractPlugin<PluginConfiguration> {

    private static final String MULTIPART_CLASS_NAME = "org.springframework.web.multipart.MultipartFile";

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode propertyNode) {
            if (propertyNode.getSource().getAssociatedTypes().stream()
                    .map(SignatureModel::get).map(Objects::toString)
                    .anyMatch(MULTIPART_CLASS_NAME::equals)) {
                throw new IllegalArgumentException(
                        "MultipartFile is not allowed in entity classes");
            }
        }

        if (nodePath.getParentPath().getNode() instanceof MethodNode && nodePath
                .getNode() instanceof TypeSignatureNode typeSignatureNode) {
            if (MULTIPART_CLASS_NAME
                    .equals(typeSignatureNode.getType().get().toString())) {
                throw new IllegalArgumentException(
                        "MultipartFile is not allowed as return type");
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
