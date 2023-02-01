package dev.hilla.parser.models.jackson;

import java.util.Optional;

import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.Model;

public interface JacksonModel<F extends Model, G extends Model, S extends Model>
        extends Model, AnnotatedModel {
    Optional<F> getField();

    Optional<G> getGetter();

    Optional<S> getSetter();
}
