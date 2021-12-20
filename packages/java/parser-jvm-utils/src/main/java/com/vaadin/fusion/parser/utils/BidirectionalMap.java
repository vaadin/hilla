package com.vaadin.fusion.parser.utils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BidirectionalMap<K, V> implements Map<K, V> {
    private transient final Map<K, V> map = new HashMap<>();
    private transient final Map<V, K> reverseMap = new HashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsReverseKey(Object key) {
        return reverseMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public boolean containsReverseValue(Object value) {
        return reverseMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    public K getReverse(Object key) {
        return reverseMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        reverseMap.put(value, key);
        return map.put(key, value);
    }

    public K putReverse(V key, K value) {
        map.put(value, key);
        return reverseMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        var removed = map.remove(key);

        if (removed != null) {
            reverseMap.remove(removed);
        }

        return removed;
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        map.putAll(m);
        m.forEach((key, value) -> reverseMap.put(value, key));
    }

    public K removeReverse(Object key) {
        var removed = reverseMap.remove(key);

        if (removed != null) {
            map.remove(removed);
        }

        return removed;
    }

    @Override
    public void clear() {
        map.clear();
        reverseMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    public Set<V> reverseKeySet() {
        return reverseMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    public Collection<K> reverseValues() {
        return reverseMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public Set<Entry<V, K>> reverseEntrySet() {
        return reverseMap.entrySet();
    }
}
