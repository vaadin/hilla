package com.vaadin.fusion.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.ArrayRelativeTypeSignature;
import com.vaadin.fusion.parser.core.ClassRefRelativeTypeSignature;
import com.vaadin.fusion.parser.core.RelativeTypeArgument;
import com.vaadin.fusion.parser.core.RelativeTypeParameter;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;

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

final class SchemaProcessor {
    private final AssociationMap associationMap;
    private final RelativeTypeSignature signature;

    public SchemaProcessor(@Nonnull RelativeTypeSignature signature,
            @Nonnull AssociationMap associationMap) {
        this.associationMap = associationMap;
        this.signature = Objects.requireNonNull(signature);
    }

    public Schema<?> process() {
        Schema<?> result;

        if (signature.isString()) {
            result = stringSchema();
        } else if (signature.isBoolean()) {
            result = booleanSchema();
        } else if (signature.hasIntegerType()) {
            result = integerSchema();
        } else if (signature.hasFloatType()) {
            result = numberSchema();
        } else if (signature.isArray()) {
            result = arraySchema();
        } else if (signature.isIterable()) {
            result = iterableSchema();
        } else if (signature.isMap()) {
            result = mapSchema();
        } else if (signature.isTypeArgument()) {
            result = typeArgumentSchema();
        } else if (signature.isTypeParameter()) {
            result = typeParameterSchema();
        } else if (signature.isDate()) {
            result = dateSchema();
        } else if (signature.isDateTime()) {
            result = dateTimeSchema();
        } else if (signature.isClassRef()) {
            result = refSchema();
        } else {
            result = anySchema();
        }

        associationMap.put(result, signature);

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArrayRelativeTypeSignature) signature)
                .getNestedType();
        var items = new SchemaProcessor(nestedType, associationMap).process();

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
        var typeArguments = ((ClassRefRelativeTypeSignature) signature)
                .getTypeArguments();

        if (typeArguments.size() > 0) {
            return schema.items(
                    new SchemaProcessor(typeArguments.get(0), associationMap)
                            .process());
        }

        // If it is a nested class with generic parameters, we have to look
        // at the suffix type arguments
        // instead of regular ones.
        var suffixTypeArguments = ((ClassRefRelativeTypeSignature) signature)
                .getSuffixTypeArguments();

        if (suffixTypeArguments.size() > 0
                && suffixTypeArguments.get(0).size() > 0) {
            return schema.items(
                    new SchemaProcessor(suffixTypeArguments.get(0).get(0),
                            associationMap).process());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var typeArguments = ((ClassRefRelativeTypeSignature) signature)
                .getTypeArguments();
        var values = new SchemaProcessor(typeArguments.get(1), associationMap)
                .process();

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
        return ((RelativeTypeArgument) signature).getWildcardAssociatedType()
                .<Schema<?>> map(
                        value -> new SchemaProcessor(value, associationMap)
                                .process())
                .orElseGet(this::anySchema);
    }

    private Schema<?> typeParameterSchema() {
        var classBound = ((RelativeTypeParameter) signature).getClassBound();

        if (classBound != null) {
            return new SchemaProcessor(classBound, associationMap).process();
        }

        var interfaceBounds = ((RelativeTypeParameter) signature)
                .getInterfaceBounds();

        return interfaceBounds.stream()
                .filter(bound -> bound.isMap() || bound.isIterable())
                .findFirst()
                .<Schema<?>> map(
                        bound -> new SchemaProcessor(bound, associationMap)
                                .process())
                .orElseGet(this::anySchema);
    }
}
