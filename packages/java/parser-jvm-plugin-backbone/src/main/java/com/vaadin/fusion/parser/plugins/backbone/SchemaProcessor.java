package com.vaadin.fusion.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.ArrayRelativeTypeSignature;
import com.vaadin.fusion.parser.core.AssociationMap;
import com.vaadin.fusion.parser.core.ClassRefRelativeTypeSignature;
import com.vaadin.fusion.parser.core.RelativeTypeArgument;
import com.vaadin.fusion.parser.core.RelativeTypeParameter;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;
import com.vaadin.fusion.parser.core.TypeVariableRelativeTypeSignature;

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
    private static final Schema<?> anySchemaSample = new ObjectSchema();

    private final AssociationMap associationMap;
    private final RelativeTypeSignature signature;

    public SchemaProcessor(@Nonnull RelativeTypeSignature signature,
            @Nonnull AssociationMap associationMap) {
        this.associationMap = associationMap;
        this.signature = Objects.requireNonNull(signature);
    }

    private static <T extends Schema<?>> T nullify(T schema,
            boolean condition) {
        return (T) schema.nullable(condition ? true : null);
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
        } else if (signature.isTypeVariable()) {
            result = typeVariableSchema();
        } else {
            result = anySchema();
        }

        associationMap.addType(result, signature);

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArrayRelativeTypeSignature) signature)
                .getNestedType();
        var items = new SchemaProcessor(nestedType, associationMap).process();

        return nullify(new ArraySchema().items(items), true);
    }

    private Schema<?> booleanSchema() {
        return nullify(new BooleanSchema(), !signature.isPrimitive());
    }

    private Schema<?> dateSchema() {
        return nullify(new DateSchema(), true);
    }

    private Schema<?> dateTimeSchema() {
        return nullify(new DateTimeSchema(), true);
    }

    private Schema<?> integerSchema() {
        return nullify(new IntegerSchema(), !signature.isPrimitive())
                .format(signature.isLong() ? "int64" : "int32");
    }

    private Schema<?> iterableSchema() {
        var schema = nullify(new ArraySchema(), true);
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

        return nullify(new MapSchema(), true).additionalProperties(values);
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !signature.isPrimitive())
                .format(signature.isDouble() ? "double" : "float");
    }

    private Schema<?> refSchema() {
        var fullyQualifiedName = ((ClassRefRelativeTypeSignature) signature)
                .get().getFullyQualifiedClassName();

        return nullify(new ComposedSchema(), true)
                .anyOf(Collections.singletonList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !signature.isPrimitive());
    }

    private Schema<?> typeArgumentSchema() {
        return ((RelativeTypeArgument) signature).getWildcardAssociatedType()
                .<Schema<?>> map(
                        value -> new SchemaProcessor(value, associationMap)
                                .process())
                .orElseGet(this::anySchema);
    }

    private Schema<?> typeParameterSchema() {
        return ((RelativeTypeParameter) signature).getClassBound()
                .filter(classBound -> !classBound.isNativeObject())
                .<Schema<?>> map(classBound -> new SchemaProcessor(classBound,
                        associationMap).process())
                .or(() -> ((RelativeTypeParameter) signature)
                        .getInterfaceBounds().stream()
                        .map(bound -> new SchemaProcessor(bound, associationMap)
                                .process())
                        .filter(schema -> !Objects.equals(schema,
                                anySchemaSample))
                        .findFirst())
                .orElseGet(this::anySchema);
    }

    private Schema<?> typeVariableSchema() {
        return new SchemaProcessor(
                ((TypeVariableRelativeTypeSignature) signature).resolve(),
                associationMap).process();
    }
}
