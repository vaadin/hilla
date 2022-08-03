package dev.hilla.parser.core;

import java.util.Objects;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.Model;

public class SignatureInfo {
    private final Model base;

    public SignatureInfo(@Nonnull Model base) {
        this.base = Objects.requireNonNull(base);
    }

    public Model getBase() {
        return base;
    }
}
