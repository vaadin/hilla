package com.vaadin.hilla.typescript.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class EntityNode extends AbstractNode<ClassInfoModel, Schema<?>> {
    private EntityNode(@NonNull ClassInfoModel source,
            @NonNull ObjectSchema target) {
        super(source, target);
    }

    @NonNull
    static public EntityNode of(@NonNull ClassInfoModel model) {
        return new EntityNode(model, new ObjectSchema());
    }
}
