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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

/**
 * The files the plugins render while the parser walks the browser callable
 * classes, kept in memory until the walk is over.
 *
 * <p>
 * A file is emitted by the plugin owning it, for the node it is written from,
 * and stays a structured document rather than text, so that the plugins
 * contributing to it can still change it: they find it through the node, not
 * through where it is written. It is only printed once the walk is over.
 *
 * <p>
 * A path holds one file. A file emitted at a path which already holds one
 * replaces it, so that the last node written to a path is the one it holds,
 * whichever order the walk reached them in.
 *
 * @deprecated Internal API, not deprecated for removal. The Java API of
 *             {@code hilla-typescript-generator} is used only by Hilla itself,
 *             and may change or be removed in any release without notice. Do
 *             not use it in application code or libraries. It is marked
 *             deprecated only to make the compiler warn about such use.
 */
@Deprecated
public final class Output {
    private final Map<String, Emitted> files = new LinkedHashMap<>();

    /**
     * Adds a file, replacing the one the path holds, if any.
     *
     * @param node
     *            the node the file is written from, which is how the plugins
     *            contributing to it find it
     * @param path
     *            the path of the file relative to the output folder, with
     *            {@code /} as the separator
     * @param document
     *            what the file holds, until it is printed
     */
    public void emit(@NonNull Node<?, ?> node, @NonNull String path,
            @NonNull Document document) {
        files.put(Objects.requireNonNull(path),
                new Emitted(Objects.requireNonNull(node),
                        Objects.requireNonNull(document)));
    }

    /**
     * The documents of the given kind which are written from a node, which is
     * what a plugin contributing to the files of the node changes.
     */
    @NonNull
    public <T extends Document> Stream<T> of(@NonNull Node<?, ?> node,
            @NonNull Class<T> type) {
        return files.values().stream()
                .filter(emitted -> emitted.node().equals(node))
                .map(Emitted::document).filter(type::isInstance)
                .map(type::cast);
    }

    /**
     * Prints every file, by its path, in the order the paths were first emitted
     * at.
     */
    @NonNull
    public Map<String, String> print() {
        var printed = new LinkedHashMap<String, String>();
        files.forEach((path, emitted) -> printed.put(path,
                emitted.document().print()));
        return printed;
    }

    /**
     * What a file holds until the walk is over and it is printed.
     */
    public interface Document {
        @NonNull
        String print();
    }

    private record Emitted(Node<?, ?> node, Document document) {
    }
}
