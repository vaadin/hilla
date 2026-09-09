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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import reactor.core.publisher.Flux;

import com.vaadin.hilla.EndpointSubscription;
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

    public Detailed detailed() {
        return null;
    }

    public Marker marker() {
        return null;
    }

    public Bounded bounded() {
        return null;
    }

    public Mixed mixed() {
        return null;
    }

    public Figure figure() {
        return null;
    }

    public Shaded shaded() {
        return null;
    }

    /**
     * Sends a series of values rather than returning one, which the client
     * subscribes to instead of calling.
     */
    public Flux<String> stream(int count) {
        return null;
    }

    public EndpointSubscription<Sample> watch() {
        return null;
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

    /**
     * An entity which inherits the properties of another one rather than
     * declaring them again.
     */
    public static class Detailed extends Sample {
        private String note;

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    /**
     * A type a value of which is one of the subtypes it declares, which is what
     * a union is written from. A subtype of a subtype accepts the id of both,
     * so that reading the discriminator of a value of the base type narrows it
     * to the right one.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Figure.Round.class, name = "round"),
            @JsonSubTypes.Type(value = Figure.Ring.class, name = "ring"),
            @JsonSubTypes.Type(value = Figure.Blank.class, name = "blank") })
    public static class Figure {
        private String label;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public static class Round extends Figure {
            private double radius;

            public double getRadius() {
                return radius;
            }

            public void setRadius(double radius) {
                this.radius = radius;
            }
        }

        /**
         * A subtype with nothing of its own, which the id it is written as
         * still tells from the others.
         */
        public static class Blank extends Figure {
        }

        public static class Ring extends Round {
            private double hole;

            public double getHole() {
                return hole;
            }

            public void setHole(double hole) {
                this.hole = hole;
            }
        }
    }

    /**
     * A hierarchy whose discriminator is a property the types declare
     * themselves, which is written as the ids rather than twice.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "shade")
    @JsonSubTypes(@JsonSubTypes.Type(value = Shaded.Pale.class, name = "pale"))
    public static class Shaded {
        private String shade;

        public String getShade() {
            return shade;
        }

        public void setShade(String shade) {
            this.shade = shade;
        }

        public static class Pale extends Shaded {
        }
    }

    /**
     * An entity with a property of every kind, so that the model of each of
     * them is written from something.
     */
    public static class Mixed {
        private int count;

        private boolean active;

        private List<String> tags;

        private Map<String, Integer> counts;

        private Kind kind;

        private Sample sample;

        private String[] words;

        private Set<String> unique;

        private Map<String, List<String>> grouped;

        private Optional<List<String>> maybeTags;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }

        public void setCounts(Map<String, Integer> counts) {
            this.counts = counts;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public Sample getSample() {
            return sample;
        }

        public void setSample(Sample sample) {
            this.sample = sample;
        }

        public String[] getWords() {
            return words;
        }

        public void setWords(String[] words) {
            this.words = words;
        }

        public Set<String> getUnique() {
            return unique;
        }

        public void setUnique(Set<String> unique) {
            this.unique = unique;
        }

        public Map<String, List<String>> getGrouped() {
            return grouped;
        }

        public void setGrouped(Map<String, List<String>> grouped) {
            this.grouped = grouped;
        }

        public Optional<List<String>> getMaybeTags() {
            return maybeTags;
        }

        public void setMaybeTags(Optional<List<String>> maybeTags) {
            this.maybeTags = maybeTags;
        }
    }

    /**
     * An entity whose type parameter is bound to another entity, which the
     * declaration stands for: TypeScript is not told what the parameter is,
     * only what it has to be.
     */
    public static class Bounded<T extends Sample> {
        private T held;

        public T getHeld() {
            return held;
        }

        public void setHeld(T held) {
            this.held = held;
        }
    }

    /**
     * An entity with no property at all, which TypeScript still needs a
     * declaration of.
     */
    public static class Marker {
    }

    public static class Sample {
        private String name;

        /**
         * Serialized under a name of its own, which is the one the generated
         * TypeScript has to use.
         */
        @JsonProperty("label")
        private String title;

        /**
         * Of the type declaring it, so that the file refers to itself.
         */
        private Sample parent;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Sample getParent() {
            return parent;
        }

        public void setParent(Sample parent) {
            this.parent = parent;
        }
    }
}
