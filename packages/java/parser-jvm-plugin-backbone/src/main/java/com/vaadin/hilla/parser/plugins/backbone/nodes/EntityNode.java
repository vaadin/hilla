package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class EntityNode extends AbstractNode<ClassInfoModel, Schema<?>> {
    protected EntityNode(@NonNull ClassInfoModel source,
            @NonNull ObjectSchema target) {
        super(source, target);
    }

    @NonNull
    static public EntityNode of(@NonNull ClassInfoModel model) {
        return new EntityNode(model, new ObjectSchema());
    }
}
