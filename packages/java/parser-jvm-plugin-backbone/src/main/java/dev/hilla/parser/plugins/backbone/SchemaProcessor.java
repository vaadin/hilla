package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.SignatureInfo;
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
    private final SignatureInfo info;
    private final SharedStorage storage;
    private final SignatureModel type;

    public SchemaProcessor(@Nonnull SignatureModel type,
            @Nonnull SignatureInfo info, @Nonnull SharedStorage storage) {
        this.storage = Objects.requireNonNull(storage);
        this.info = Objects.requireNonNull(info);
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
        } else if (type.hasFloatType() || type.isBigDecimal()) {
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

        storage.getAssociationMap().addSignature(result, type, info);

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArraySignatureModel) type).getNestedType();
        var items = new SchemaProcessor(nestedType, info, storage).process();

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
        var _type = (ClassRefSignatureModel) type;
        var typeArguments = _type.getTypeArguments();

        if (!typeArguments.isEmpty()) {
            schema = schema.items(
                    new SchemaProcessor(typeArguments.get(0), info, storage)
                            .process());
        }

        if (type.isNonJDKClass()) {
            schema.addExtension("x-class-name", _type.getName());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var schema = nullify(new MapSchema(), true);

        var _type = (ClassRefSignatureModel) type;
        var typeArguments = _type.getTypeArguments();

        // For the TS generator, to recognize a schema as a map, it requires
        // "additionalProperties" to be set
        schema.additionalProperties(!typeArguments.isEmpty()
                ? new SchemaProcessor(typeArguments.get(1), info, storage)
                        .process()
                : anySchema());

        if (type.isNonJDKClass()) {
            schema.addExtension("x-class-name", _type.getName());
        }

        return schema;
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !type.isPrimitive())
                .format(type.isFloat() ? "float" : "double");
    }

    private Schema<?> optionalSchema() {
        var _type = (ClassRefSignatureModel) type;
        var typeArguments = _type.getTypeArguments();

        return !typeArguments.isEmpty()
                ? new SchemaProcessor(typeArguments.get(0), info, storage)
                        .process()
                : anySchema();
    }

    private Schema<?> refSchema() {
        if (type.isJDKClass()) {
            return anySchema();
        }

        var _type = (ClassRefSignatureModel) type;
        var fullyQualifiedName = _type.getClassInfo().getName();

        return nullify(new ComposedSchema(), true)
                .anyOf(Collections.singletonList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !type.isPrimitive());
    }

    private Schema<?> typeArgumentSchema() {
        var types = ((TypeArgumentModel) type).getAssociatedTypes();

        return !types.isEmpty()
                ? new SchemaProcessor(types.get(0), info, storage).process()
                : anySchema();
    }

    private Schema<?> typeParameterSchema() {
        return ((TypeParameterModel) type).getBounds().stream()
                .filter(Objects::nonNull)
                .filter(bound -> !bound.isNativeObject())
                .<Schema<?>> map(
                        bound -> new SchemaProcessor(bound, info, storage)
                                .process())
                .filter(schema -> !Objects.equals(schema, anySchemaSample))
                .findFirst().orElseGet(this::anySchema);
    }

    private Schema<?> typeVariableSchema() {
        return new SchemaProcessor(((TypeVariableModel) type).resolve(), info,
                storage).process();
    }
}
