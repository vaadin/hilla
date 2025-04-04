package com.vaadin.hilla.parser.plugins.transfertypes;

import org.jspecify.annotations.NonNull;

record Import(@NonNull String module, @NonNull String specifier, @NonNull String type) implements TransferType {
    static Import NAMED(@NonNull String module, @NonNull String specifier) {
        return new Import(module, specifier, "named");
    }

    static Import DEFAULT(@NonNull String module, @NonNull String specifier) {
        return new Import(module, specifier, "default");
    }
}
