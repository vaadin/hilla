package com.vaadin.hilla.parser.plugins.backbone.iterable;

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

    public static class Foo {
        public String bar = "bar";
    }
}
