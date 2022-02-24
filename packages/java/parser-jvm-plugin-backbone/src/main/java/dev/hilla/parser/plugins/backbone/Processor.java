package dev.hilla.parser.plugins.backbone;

import java.util.Collection;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.core.RelativeClassInfo;
import io.swagger.v3.oas.models.OpenAPI;

abstract class Processor {
    protected final AssociationMap associationMap;
    protected final Collection<RelativeClassInfo> classes;
    protected final OpenAPI model;

    public Processor(@Nonnull Collection<RelativeClassInfo> classes,
            @Nonnull OpenAPI model, @Nonnull AssociationMap associationMap) {
        this.associationMap = associationMap;
        this.classes = classes;
        this.model = model;
    }

    public abstract void process();
}
