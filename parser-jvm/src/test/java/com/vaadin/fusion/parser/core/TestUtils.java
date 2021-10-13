package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
    public final static class PluginElementsFilter {
        private final String base;

        public PluginElementsFilter(String base) {
            this.base = base;
        }

        public List<RelativeClassInfo> apply(List<RelativeClassInfo> elements) {
            return elements.stream()
                    .filter(element -> element.get().getName().contains(base))
                    .collect(Collectors.toList());
        }
    }
}
