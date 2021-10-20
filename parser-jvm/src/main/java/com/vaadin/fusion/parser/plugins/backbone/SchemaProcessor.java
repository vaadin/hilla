package com.vaadin.fusion.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.fusion.parser.core.ArrayRelativeTypeSignature;
import com.vaadin.fusion.parser.core.ClassRefRelativeTypeSignature;
import com.vaadin.fusion.parser.core.RelativeTypeArgument;
import com.vaadin.fusion.parser.core.RelativeTypeParameter;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class SchemaProcessor {
    private final RelativeTypeSignature signature;

    public SchemaProcessor(RelativeTypeSignature signature) {
        Objects.requireNonNull(signature);
        this.signature = signature;
    }

    public Schema<?> process() {
        if (signature.isString()) {
            return stringSchema();
        } else if (signature.isBoolean()) {
            return booleanSchema();
        } else if (signature.isNumber()) {
            return numberSchema();
        } else if (signature.isArray()) {
            return arraySchema();
        } else if (signature.isIterable()) {
            return iterableSchema();
        } else if (signature.isMap()) {
            return mapSchema();
        } else if (signature.isTypeArgument()) {
            return typeArgumentSchema();
        } else if (signature.isTypeParameter()) {
            return typeParameterSchema();
        } else if (signature.isClassRef()) {
            return refSchema();
        }

        return anySchema();
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        Schema<?> items = new SchemaProcessor(
                ((ArrayRelativeTypeSignature) signature).getNestedType())
                        .process();

        return new ArraySchema().items(items).nullable(true);
    }

    private Schema<?> refSchema() {
        String fullyQualifiedName = ((ClassRefRelativeTypeSignature) signature)
                .get().getFullyQualifiedClassName();

        return new ComposedSchema()
                .anyOf(Collections.singletonList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)))
                .nullable(true);
    }

    private Schema<?> booleanSchema() {
        return new BooleanSchema().nullable(!signature.isPrimitive());
    }

    private Schema<?> iterableSchema() {
        Schema<?> items = new SchemaProcessor(
                ((ClassRefRelativeTypeSignature) signature).getTypeArguments()
                        .get(0)).process();

        return new ArraySchema().items(items).nullable(true);
    }

    private Schema<?> mapSchema() {
        Schema<?> values = new SchemaProcessor(
                ((ClassRefRelativeTypeSignature) signature).getTypeArguments()
                        .get(1)).process();

        return new MapSchema().additionalProperties(values).nullable(true);
    }

    private Schema<?> numberSchema() {
        return new NumberSchema().nullable(!signature.isPrimitive());
    }

    private Schema<?> stringSchema() {
        return new StringSchema().nullable(true);
    }

    private Schema<?> typeArgumentSchema() {
        Optional<Schema<?>> schema = ((RelativeTypeArgument) signature)
                .getWildcardAssociatedType()
                .map(value -> new SchemaProcessor(value).process());

        // Optional chain is split because otherwise there is a wildcard
        // capture error
        return schema.orElseGet(this::anySchema);
    }

    private Schema<?> typeParameterSchema() {
        RelativeTypeSignature classBound = ((RelativeTypeParameter) signature)
                .getClassBound();

        if (classBound != null) {
            return new SchemaProcessor(classBound).process();
        }

        List<RelativeTypeSignature> interfaceBounds = ((RelativeTypeParameter) signature)
                .getInterfaceBounds();

        Optional<Schema<?>> schema = interfaceBounds.stream()
                .filter(bound -> bound.isMap() || bound.isIterable())
                .findFirst().map(bound -> new SchemaProcessor(bound).process());

        // Optional chain is split because otherwise there is a wildcard
        // capture error
        return schema.orElseGet(this::anySchema);
    }
}
