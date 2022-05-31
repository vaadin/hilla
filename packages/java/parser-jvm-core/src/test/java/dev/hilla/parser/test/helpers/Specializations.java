package dev.hilla.parser.test.helpers;

import java.util.Map;

public final class Specializations {
    private final Map<String, String[]> map;

    private Specializations(Map<String, String[]> map) {
        this.map = map;
    }

    public static Map.Entry<String, String[]> entry(String name,
            String... specializations) {
        return Map.entry(name, specializations);
    }

    @SafeVarargs
    public static Specializations of(Map.Entry<String, String[]>... entries) {
        return new Specializations(Map.ofEntries(entries));
    }

    public String[] get(String name) {
        return map.get(name);
    }
}
