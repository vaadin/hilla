package com.vaadin.fusion.parser.utils;

import java.util.Map;
import java.util.function.Supplier;

public interface ExtendedMap<K, V> extends Map<K, V> {
  default V getOrCreateWithDefault(K key, Supplier<V> defaultValueSupplier) {
    if (containsKey(key)) {
      return get(key);
    }

    V value = defaultValueSupplier.get();
    put(key, value);
    return value;
  }
}
