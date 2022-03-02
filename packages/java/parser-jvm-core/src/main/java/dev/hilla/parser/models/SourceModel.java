package dev.hilla.parser.models;

public interface SourceModel extends Model {
    @Override
    default boolean isSource() {
        return true;
    }
}
