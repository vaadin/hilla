package dev.hilla.parser.models;

public interface SourceModel extends TypeModel {
    @Override
    default boolean isSourceType() {
        return true;
    }
}
