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
package com.vaadin.hilla.parser.plugins.backbone.iterable;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Endpoint
public class IterableEndpoint {
    public AnotherCustomIterable<Foo> getFooAnotherCustomIterable() {
        return new AnotherCustomIterable<>();
    }

    public Foo[] getFooArray() {
        return new Foo[] {};
    }

    public CustomIterable<Foo> getFooCustomIterable() {
        return new CustomIterable<>();
    }

    public Iterable<Foo> getFooIterable() {
        return Arrays.asList(new Foo(), new Foo());
    }

    public SpecializedIterable getSpecializedIterable() {
        return new SpecializedIterable();
    }

    public SpecializedIterableCustom getSpecializedIterableCustom() {
        return new SpecializedIterableCustom();
    }

    public List<Foo> getFooList() {
        return new ArrayList<>();
    }

    public Set<Foo> getFooSet() {
        return new HashSet<>();
    }

    public static class AnotherCustomIterable<T> implements Iterable<T> {
        private final List<T> list = new ArrayList<>();

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }
    }

    public static class CustomIterable<T> implements Iterable<T> {
        private final List<T> list = new ArrayList<>();

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }
    }

    public static class SpecializedIterable extends ArrayList<String> {
    }

    public static class SpecializedIterableCustom extends ArrayList<Foo> {
    }

    public static class Foo {
        public String bar = "bar";
    }
}
