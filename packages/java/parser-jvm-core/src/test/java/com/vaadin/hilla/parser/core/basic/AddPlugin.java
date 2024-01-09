package com.vaadin.hilla.parser.core.basic;

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.cleanup;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.MethodInfoModel;
import com.vaadin.hilla.parser.test.nodes.EndpointNode;
import com.vaadin.hilla.parser.test.nodes.EntityNode;
import com.vaadin.hilla.parser.test.nodes.FieldNode;
import com.vaadin.hilla.parser.test.nodes.MethodNode;

final class AddPlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            var endpoints = rootNode.getSource()
                    .getClassesWithAnnotation(getStorage().getParserConfig()
                            .getEndpointAnnotationName())
                    .stream().map(ClassInfoModel::of)
                    .collect(Collectors.toList());
            return nodeDependencies
                    .appendChildNodes(endpoints.stream().map(EndpointNode::of))
                    .appendRelatedNodes(endpoints.stream()
                            .map(ClassInfoModel::getInnerClasses)
                            .flatMap(Collection::stream).map(EntityNode::of));
        } else if (node instanceof EndpointNode) {
            return nodeDependencies
                    .appendChildNodes(Stream.concat(
                            ((EndpointNode) node).getSource().getFields()
                                    .stream().map(FieldNode::of),
                            cleanup(((EndpointNode) node).getSource()
                                    .getMethods().stream())
                                    .map(MethodNode::of)))
                    .appendRelatedNodes(Stream.of(
                            EntityNode.of(ClassInfoModel.of(Sample.class))));
        }

        if (node instanceof EntityNode && ((EntityNode) node).getSource()
                .getName().equals(Sample.class.getName())) {
            return nodeDependencies
                    .appendChildNodes(Stream
                            .of(getDeclaredMethod(Sample.class, "methodFoo"),
                                    getDeclaredMethod(Sample.class,
                                            "methodBar"))
                            .map(MethodInfoModel::of).map(MethodNode::of));
        }

        return nodeDependencies;
    }

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
