package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import java.util.Objects;

import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;

public final class ScanItem {
    private final Model model;
    private final String kind;

    public ScanItem(@Nonnull Model model, @Nonnull String kind) {
        this.model = Objects.requireNonNull(model);
        this.kind = Objects.requireNonNull(kind);
    }

    public Model getModel() {
        return model;
    }

    public String getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof ScanItem)) {
            return false;
        }

        return ((ScanItem) another).getModel().equals(model) &&
            ((ScanItem) another).getKind().equals(kind);
    }

    @Override
    public int hashCode() {
        return model.hashCode() ^ kind.hashCode();
    }

    public String toString() {
        return String.format("%s %s", kind, ((NamedModel) model).getName());
    }
}
