package com.vaadin.fusion.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import com.vaadin.fusion.parser.core.ArrayRelativeTypeSignature;
import com.vaadin.fusion.parser.core.ClassRefRelativeTypeSignature;
import com.vaadin.fusion.parser.core.RelativeTypeArgument;
import com.vaadin.fusion.parser.core.RelativeTypeParameter;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

final class SchemaProcessor {
    private final RelativeTypeSignature signature;

    public SchemaProcessor(@Nonnull RelativeTypeSignature signature) {
        this.signature = Objects.requireNonNull(signature);
    }

    public Schema<?> process() {
        if (signature.isString()) {
            return stringSchema();
        } else if (signature.isBoolean()) {
            return booleanSchema();
        } else if (signature.hasIntegerType()) {
            return integerSchema();
        } else if (signature.hasFloatType()) {
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
        } else if (signature.isDate()) {
            return dateSchema();
        } else if (signature.isDateTime()) {
            return dateTimeSchema();
        } else if (signature.isClassRef()) {
            return refSchema();
        }

        return anySchema();
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArrayRelativeTypeSignature) signature).getNestedType();
        var items = new SchemaProcessor(nestedType).process();

        return new ArraySchema().items(items).nullable(true);
    }

    private Schema<?> booleanSchema() {
        return new BooleanSchema().nullable(!signature.isPrimitive());
    }

    private Schema<?> dateSchema() {
        return new DateSchema();
    }

    private Schema<?> dateTimeSchema() {
        return new DateTimeSchema();
    }

    private Schema<?> integerSchema() {
        return new IntegerSchema().nullable(!signature.isPrimitive())
            .format(signature.isLong() ? "int64" : "int32");
    }

    private Schema<?> iterableSchema() {
        var schema = (ArraySchema) new ArraySchema().nullable(true);
        var typeArguments = ((ClassRefRelativeTypeSignature) signature).getTypeArguments();

        if (typeArguments.size() > 0) {
            return schema.items(new SchemaProcessor(typeArguments.get(0)).process());
        }

        // If it is a nested class with generic parameters, we have to look
        // at the suffix type arguments
        // instead of regular ones.
        var suffixTypeArguments = ((ClassRefRelativeTypeSignature) signature)
            .getSuffixTypeArguments();

        if (suffixTypeArguments.size() > 0
            && suffixTypeArguments.get(0).size() > 0) {
            return schema.items(
                new SchemaProcessor(suffixTypeArguments.get(0).get(0))
                    .process());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var typeArguments = ((ClassRefRelativeTypeSignature) signature).getTypeArguments();
        var values = new SchemaProcessor(
            typeArguments.get(1)).process();

        return new MapSchema().additionalProperties(values).nullable(true);
    }

    private Schema<?> numberSchema() {
        return new NumberSchema().nullable(!signature.isPrimitive())
            .format(signature.isDouble() ? "double" : "float");
    }

    private Schema<?> refSchema() {
        var fullyQualifiedName = ((ClassRefRelativeTypeSignature) signature)
            .get().getFullyQualifiedClassName();

        return new ComposedSchema()
            .anyOf(Collections.singletonList(new Schema<>()
                .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)))
            .nullable(true);
    }

    private Schema<?> stringSchema() {
        return new StringSchema().nullable(!signature.isPrimitive());
    }

    private Schema<?> typeArgumentSchema() {
        return ((RelativeTypeArgument) signature)
            .getWildcardAssociatedType()
            .<Schema<?>>map(value -> new SchemaProcessor(value).process()).orElseGet(this::anySchema);
    }

    private Schema<?> typeParameterSchema() {
        var classBound = ((RelativeTypeParameter) signature).getClassBound();

        if (classBound != null) {
            return new SchemaProcessor(classBound).process();
        }

        var interfaceBounds = ((RelativeTypeParameter) signature).getInterfaceBounds();

        return interfaceBounds.stream()
            .filter(bound -> bound.isMap() || bound.isIterable())
            .findFirst().<Schema<?>>map(bound -> new SchemaProcessor(bound).process()).orElseGet(this::anySchema);
    }
}
