package com.vaadin.hilla.parser.plugins.transfertypes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;

public class MultipartFileCheckerPlugin
        extends AbstractPlugin<PluginConfiguration> {

    private static final String MULTIPART_CLASS_NAME = "org.springframework.web.multipart.MultipartFile";

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode propertyNode) {
            var nodeType = propertyNode.getSource().get().getRawPrimaryType();
            if (ClassInfoModel.isAssignableFrom(MULTIPART_CLASS_NAME,
                    nodeType)) {
                throw new IllegalArgumentException(
                        "MultipartFile is not allowed in entity classes");
            }
        }

        if (nodePath.getNode() instanceof MethodNode methodNode) {
            var nodeType = methodNode.getSource().getResultType();
            if (nodeType instanceof ClassRefSignatureModel classRefSignatureModel
                    && ClassInfoModel.isAssignableFrom(MULTIPART_CLASS_NAME,
                            classRefSignatureModel.getClassInfo())) {
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
