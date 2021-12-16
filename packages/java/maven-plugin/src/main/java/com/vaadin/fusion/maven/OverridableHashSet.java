package com.vaadin.fusion.maven;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

class OverridableHashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, Serializable {
    private transient HashMap<Integer, E> map;

    public OverridableHashSet() {
        map = new HashMap<>();
    }

    public OverridableHashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }

    public OverridableHashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    public OverridableHashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    @Override
    public OverridableHashSet<E> clone() {
        try {
            var clone = (OverridableHashSet<E>) super.clone();
            clone.map = (HashMap<Integer, E>) map.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o.hashCode());
    }

    @Override
    public Iterator<E> iterator() {
        return map.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.values().toArray();
    }

    @Override
    public <T> T[] toArray(@Nonnull T[] a) {
        return map.values().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return map.put(e.hashCode(), e) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o.hashCode()) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return c.stream().anyMatch(this::add);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return map.values().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return map.values().removeAll(c);
    }

    @Override
    public void clear() {
        map.clear();
    }
}
