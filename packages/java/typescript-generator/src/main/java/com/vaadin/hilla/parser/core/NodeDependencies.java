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
package com.vaadin.hilla.parser.core;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

public final class NodeDependencies {
    private final Stream<Node<?, ?>> childNodes;
    private final Node<?, ?> node;
    private final Stream<Node<?, ?>> relatedNodes;

    NodeDependencies(@NonNull Node<?, ?> node,
            @NonNull Stream<Node<?, ?>> childNodes,
            @NonNull Stream<Node<?, ?>> relatedNodes) {
        this.node = Objects.requireNonNull(node);
        this.childNodes = Objects.requireNonNull(childNodes);
        this.relatedNodes = Objects.requireNonNull(relatedNodes);
    }

    @NonNull
    public NodeDependencies appendChildNodes(
            @NonNull Stream<Node<?, ?>> childNodesToAppend) {
        return withChildNodes(
                Stream.concat(getChildNodes(), childNodesToAppend));
    }

    @NonNull
    public NodeDependencies appendRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodesToAppend) {
        return withRelatedNodes(
                Stream.concat(getRelatedNodes(), relatedNodesToAppend));
    }

    public Stream<Node<?, ?>> getChildNodes() {
        return childNodes;
    }

    public Node<?, ?> getNode() {
        return node;
    }

    public Stream<Node<?, ?>> getRelatedNodes() {
        return relatedNodes;
    }

    @NonNull
    public NodeDependencies prependChildNodes(
            @NonNull Stream<Node<?, ?>> childNodesToPrepend) {
        return withChildNodes(
                Stream.concat(childNodesToPrepend, getChildNodes()));
    }

    @NonNull
    public NodeDependencies prependRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodesToPrepend) {
        return withRelatedNodes(
                Stream.concat(relatedNodesToPrepend, getRelatedNodes()));
    }

    @NonNull
    public NodeDependencies processChildNodes(
            @NonNull UnaryOperator<Stream<Node<?, ?>>> childNodesProcessor) {
        return new NodeDependencies(getNode(),
                childNodesProcessor.apply(getChildNodes()), getRelatedNodes());
    }

    @NonNull
    public NodeDependencies processRelatedNodes(
            @NonNull UnaryOperator<Stream<Node<?, ?>>> relatedNodesProcessor) {
        return new NodeDependencies(getNode(), getChildNodes(),
                relatedNodesProcessor.apply(getRelatedNodes()));
    }

    @NonNull
    private NodeDependencies withChildNodes(
            @NonNull Stream<Node<?, ?>> childNodes) {
        return new NodeDependencies(getNode(), childNodes, getRelatedNodes());
    }

    @NonNull
    private NodeDependencies withRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodes) {
        return new NodeDependencies(getNode(), getChildNodes(), relatedNodes);
    }

}
