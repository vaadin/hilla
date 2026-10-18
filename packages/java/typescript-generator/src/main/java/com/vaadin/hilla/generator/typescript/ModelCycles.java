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
package com.vaadin.hilla.generator.typescript;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Tells which references between the models of the generated types have to be
 * read lazily.
 *
 * <p>
 * A model is built as its module is evaluated, so a model which refers to a
 * model that refers back to it would read the import of a module which is not
 * evaluated yet. Only a reference taking part in such a cycle is deferred; the
 * others stay direct, which keeps the type of the model they are part of
 * navigable.
 */
final class ModelCycles {
    /**
     * The cycles of a file which refers to no model that could refer back to
     * it.
     */
    static final ModelCycles NONE = new ModelCycles(Map.of());

    private final Map<String, Set<String>> references;
    private final Map<String, Set<String>> reachable = new HashMap<>();

    private ModelCycles(Map<String, Set<String>> references) {
        this.references = references;
    }

    /**
     * The cycles between the models of the given types: a type refers to the
     * type it inherits from and to every type its properties hold, at any
     * depth.
     */
    static ModelCycles of(List<EntityModel> entities) {
        var references = new HashMap<String, Set<String>>();

        entities.forEach(entity -> references.put(entity.javaClass(),
                referencesOf(entity)));

        return new ModelCycles(references);
    }

    /**
     * Whether a property of the model of {@code owner} holding the model of
     * {@code target} has to read it lazily.
     */
    boolean isDeferred(String owner, String target) {
        return owner != null
                && (owner.equals(target) || reach(target).contains(owner));
    }

    private Set<String> reach(String from) {
        var known = reachable.get(from);

        if (known != null) {
            return known;
        }

        var result = new HashSet<String>();
        var queue = new ArrayDeque<>(references.getOrDefault(from, Set.of()));

        while (!queue.isEmpty()) {
            var next = queue.pop();

            if (result.add(next)) {
                queue.addAll(references.getOrDefault(next, Set.of()));
            }
        }

        reachable.put(from, result);
        return result;
    }

    private static Set<String> referencesOf(EntityModel entity) {
        var found = new HashSet<String>();

        if (entity instanceof EntityModel.Bean bean) {
            bean.superTypes().forEach(type -> collect(type, found));
            bean.properties()
                    .forEach(property -> collect(property.type(), found));
        }

        return found;
    }

    private static void collect(TypeModel type, Set<String> found) {
        var nested = switch (type) {
        case TypeModel.Scalar scalar -> Stream.<TypeModel> empty();
        case TypeModel.ArrayOf array -> Stream.of(array.items());
        case TypeModel.MapOf map -> Stream.of(map.values());
        case TypeModel.EntityRef entity -> {
            found.add(entity.javaClass());
            yield entity.typeArguments().stream();
        }
        // The model of a type the framework provides is not generated, so it
        // refers to nothing, whatever it holds
        case TypeModel.Provided provided -> Stream.<TypeModel> empty();
        case TypeModel.TypeVariable variable -> Stream.<TypeModel> empty();
        };

        nested.forEach(inner -> collect(inner, found));
    }
}
