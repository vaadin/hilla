package com.vaadin.fusion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NonnullEntity {
    @Nonnull
    private final List<@Nonnull String> nonNullableField = new ArrayList<>();

    @Nonnull
    public String nonNullableMethod(
            @Nonnull Map<String, @Nonnull String> nonNullableParameter) {
        return nonNullableParameter.getOrDefault("test", "");
    }
}
