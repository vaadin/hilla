package com.vaadin.hilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

public class NonnullEntity {
    @NonNull
    private final List<@NonNull String> nonNullableField = new ArrayList<>();

    @NonNull
    public String nonNullableMethod(
            @NonNull Map<String, @NonNull String> nonNullableParameter) {
        return nonNullableParameter.getOrDefault("test", "");
    }
}
