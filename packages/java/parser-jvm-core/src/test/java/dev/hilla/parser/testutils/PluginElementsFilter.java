package dev.hilla.parser.testutils;

import java.util.List;
import java.util.stream.Collectors;

import dev.hilla.parser.core.RelativeClassInfo;

public final class PluginElementsFilter {
    private final String base;

    public PluginElementsFilter(String base) {
        this.base = base;
    }

    public List<RelativeClassInfo> apply(List<RelativeClassInfo> elements) {
        return elements.stream().filter(
                element -> element.get().getSimpleName().startsWith(base))
                .collect(Collectors.toList());
    }
}
