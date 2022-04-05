package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ArraySignatureModel;
import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.models.TypeArgumentModel;
import dev.hilla.parser.models.TypeParameterModel;
import dev.hilla.parser.models.TypeVariableModel;

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

    private final SharedStorage storage;
    private final SignatureModel type;

    public SchemaProcessor(@Nonnull SignatureModel type,
            @Nonnull SharedStorage storage) {
        this.storage = Objects.requireNonNull(storage);

        this.type = Objects
                .requireNonNull(type) instanceof ClassRefSignatureModel
                        ? storage.getClassMappers()
                                .map((ClassRefSignatureModel) type)
                        : type;
    }

    private static <T extends Schema<?>> T nullify(T schema,
            boolean condition) {
        return (T) schema.nullable(condition ? true : null);
    }

    public Schema<?> process() {
        Schema<?> result;

        if (type.isCharacter() || type.isString()) {
            result = stringSchema();
        } else if (type.isBoolean()) {
            result = booleanSchema();
        } else if (type.hasIntegerType()) {
            result = integerSchema();
        } else if (type.hasFloatType()) {
            result = numberSchema();
        } else if (type.isArray()) {
            result = arraySchema();
        } else if (type.isIterable()) {
            result = iterableSchema();
        } else if (type.isMap()) {
            result = mapSchema();
        } else if (type.isOptional()) {
            result = optionalSchema();
        } else if (type.isTypeArgument()) {
            result = typeArgumentSchema();
        } else if (type.isTypeParameter()) {
            result = typeParameterSchema();
        } else if (type.isDate()) {
            result = dateSchema();
        } else if (type.isDateTime()) {
            result = dateTimeSchema();
        } else if (type.isClassRef()) {
            result = refSchema();
        } else if (type.isTypeVariable()) {
            result = typeVariableSchema();
        } else {
            result = anySchema();
        }

        storage.getAssociationMap().addType(result, type);

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArraySignatureModel) type).getNestedType();
        var items = new SchemaProcessor(nestedType, storage).process();

        return nullify(new ArraySchema().items(items), true);
    }

    private Schema<?> booleanSchema() {
        return nullify(new BooleanSchema(), !type.isPrimitive());
    }

    private Schema<?> dateSchema() {
        return nullify(new DateSchema(), true);
    }

    private Schema<?> dateTimeSchema() {
        return nullify(new DateTimeSchema(), true);
    }

    private Schema<?> integerSchema() {
        return nullify(new IntegerSchema(), !type.isPrimitive())
                .format(type.isLong() ? "int64" : "int32");
    }

    private Schema<?> iterableSchema() {
        var schema = nullify(new ArraySchema(), true);
        var typeArguments = ((ClassRefSignatureModel) type).getTypeArguments();

        if (typeArguments.size() > 0) {
            return schema
                    .items(new SchemaProcessor(typeArguments.get(0), storage)
                            .process());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var typeArguments = ((ClassRefSignatureModel) type).getTypeArguments();
        var values = new SchemaProcessor(typeArguments.get(1), storage)
                .process();

        return nullify(new MapSchema(), true).additionalProperties(values);
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !type.isPrimitive())
                .format(type.isDouble() ? "double" : "float");
    }

    private Schema<?> optionalSchema() {
        var typeArguments = ((ClassRefSignatureModel) type).getTypeArguments();

        return new SchemaProcessor(typeArguments.get(0), storage).process();
    }

    private Schema<?> refSchema() {
        if (type.isJDKClass()) {
            return anySchema();
        }

        var fullyQualifiedName = ((ClassRefSignatureModel) type).resolve()
                .getName();

        return nullify(new ComposedSchema(), true)
                .anyOf(Collections.singletonList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !type.isPrimitive());
    }

    private Schema<?> typeArgumentSchema() {
        var types = ((TypeArgumentModel) type).getAssociatedTypes();

        return types.size() > 0
                ? new SchemaProcessor(types.get(0), storage).process()
                : anySchema();
    }

    private Schema<?> typeParameterSchema() {
        return ((TypeParameterModel) type).getBounds().stream()
                .filter(Objects::nonNull)
                .filter(bound -> !bound.isNativeObject())
                .<Schema<?>> map(
                        bound -> new SchemaProcessor(bound, storage).process())
                .filter(schema -> !Objects.equals(schema, anySchemaSample))
                .findFirst().orElseGet(this::anySchema);
    }

    private Schema<?> typeVariableSchema() {
        return new SchemaProcessor(((TypeVariableModel) type).resolve(),
                storage).process();
    }
}
