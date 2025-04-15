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

    // use a dedicated type `Bar` instead of reusing `Foo` to make sure it's
    // added to the OpenAPI schema
    public IterableWithProperties<Bar> getIterableWithProperties() {
        return new IterableWithProperties<>();
    }

    public static class IterableWithProperties<T> implements Iterable<T> {
        private final List<T> list = new ArrayList<>();
        private T defaultItem;

        public int size() {
            return list.size();
        }

        public T getDefaultItem() {
            return defaultItem;
        }

        public void setDefaultItem(T defaultItem) {
            this.defaultItem = defaultItem;
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }
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

    // Used in `getIterableWithProperties` to make sure it's added to the
    // OpenAPI schema. Don't use it elsewhere.
    public static class Bar {
        public String foo = "foo";
    }
}
