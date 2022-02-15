package dev.hilla.parser.models;

public interface SourceModel extends Origin {
    @Override
    default boolean isSource() {
        return true;
    }
}
