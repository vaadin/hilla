package com.vaadin.fusion.parser.plugins.backbone;

import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

class SchemaProcessor {
    private final RelativeTypeSignature signature;

    public SchemaProcessor(RelativeTypeSignature signature) {
        this.signature = signature;
    }

    public Schema<?> process() {
        if (signature.isArray()) {
            return arraySchema().nullable(true);
        } else if (signature.isPrimitive()) {

        }

        // TODO: REMOVE
        return new ArraySchema();
    }

    private Schema<?> arraySchema() {
        ArraySchema schema = new ArraySchema();

        return schema;
    }

    private Schema<?> nullify(Schema<?> schema) {
        return signature.isBase() ? schema : schema.nullable(true);
    }
}
