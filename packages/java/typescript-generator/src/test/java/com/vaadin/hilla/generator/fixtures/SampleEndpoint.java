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
package com.vaadin.hilla.generator.fixtures;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class SampleEndpoint {
    public void ping() {
    }

    public int count() {
        return 0;
    }

    public String greet(String name) {
        return name;
    }

    public List<String> names() {
        return List.of();
    }

    public Map<String, Integer> counts() {
        return Map.of();
    }

    public Sample find(String id) {
        return null;
    }

    public List<Sample> all() {
        return List.of();
    }

    public Wrapper<Sample> wrapped() {
        return null;
    }

    public Kind kind() {
        return null;
    }

    public String describe(String firstName, String lastName, int age) {
        return firstName + lastName + age;
    }

    /**
     * Annotated as always having a value, both what it returns and what it
     * takes, which the writers have to tell apart from the rest.
     */
    @Nonnull
    public String required(@Nonnull String name) {
        return name;
    }

    /**
     * Holds a value or does not, which TypeScript has nothing of its own for.
     */
    public Optional<String> maybe() {
        return Optional.empty();
    }

    public Optional<List<String>> maybeNames() {
        return Optional.empty();
    }

    public Optional<Map<String, Integer>> maybeCounts() {
        return Optional.empty();
    }

    public enum Kind {
        ONE, OTHER
    }

    /**
     * An entity with a type parameter, which the type it is used with is
     * written into.
     */
    public static class Wrapper<T> {
        private T value;

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    public static class Sample {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
