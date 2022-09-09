package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.FieldInfoModel;
import io.swagger.v3.oas.models.media.Schema;

public final class FieldNode extends AbstractNode<FieldInfoModel, String> {
    private FieldNode(@Nonnull FieldInfoModel source,
        @Nonnull String target) {
        super(source, target);
    }

    static public FieldNode of(@Nonnull FieldInfoModel source) {
        return new FieldNode(source, "");
    }
}
