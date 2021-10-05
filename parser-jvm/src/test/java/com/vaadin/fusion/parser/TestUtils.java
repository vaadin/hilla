package com.vaadin.fusion.parser;

import java.util.stream.Collectors;

public class TestUtils {
    public final static class PluginElementsFilter {
        private final String base;

        public PluginElementsFilter(final String base) {
            this.base = base;
        }

        public RelativeClassList apply(final RelativeClassList elements) {
            return elements.stream()
                    .filter(element -> element.get().getName().contains(base))
                    .collect(Collectors.toCollection(RelativeClassList::new));
        }
    }
}
