package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.Collections;
import java.util.Objects;

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
    private final SignatureModel signature;
    private final Context context;

    public SchemaProcessor(SignatureModel signature, Context context) {
        this.signature = signature;
        this.context = context;
    }

    private static <T extends Schema<?>> T nullify(T schema,
            boolean condition) {
        return (T) schema.nullable(condition ? true : null);
    }

    public Schema<?> process() {
        Schema<?> result;

        if (signature.isCharacter() || signature.isString()) {
            result = stringSchema();
        } else if (signature.isBoolean()) {
            result = booleanSchema();
        } else if (signature.hasIntegerType()) {
            result = integerSchema();
        } else if (signature.hasFloatType() || signature.isBigDecimal()) {
            result = numberSchema();
        } else if (signature.isArray()) {
            result = arraySchema();
        } else if (signature.isIterable()) {
            result = iterableSchema();
        } else if (signature.isMap()) {
            result = mapSchema();
        } else if (signature.isOptional()) {
            result = optionalSchema();
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

        context.getAssociationMap().addSignature(signature, result);

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        var nestedType = ((ArraySignatureModel) signature).getNestedType();
        var items = new SchemaProcessor(nestedType, context).process();

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
        var typeArguments = ((ClassRefSignatureModel) signature).getTypeArguments();

        if (typeArguments.size() > 0) {
            return schema.items(
                    new SchemaProcessor(typeArguments.get(0), context)
                            .process());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var _type = (ClassRefSignatureModel) signature;
        var typeArguments = _type.getTypeArguments();
        var values = new SchemaProcessor(typeArguments.get(1), context)
                .process();

        var schema = nullify(new MapSchema(), true)
                .additionalProperties(values);

        if (signature.isNonJDKClass()) {
            schema.addExtension("x-classname", _type.getName());
        }

        return schema;
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !signature.isPrimitive())
                .format(signature.isFloat() ? "float" : "double");
    }

    private Schema<?> optionalSchema() {
        var typeArguments = ((ClassRefSignatureModel) signature).getTypeArguments();

        return new SchemaProcessor(typeArguments.get(0), context)
                .process();
    }

    private Schema<?> refSchema() {
        if (signature.isJDKClass()) {
            return anySchema();
        }

        var fullyQualifiedName = ((ClassRefSignatureModel) signature).getClassInfo()
                .getName();

        return nullify(new ComposedSchema(), true)
                .anyOf(Collections.singletonList(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName)));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !signature.isPrimitive());
    }

    private Schema<?> typeArgumentSchema() {
        var types = ((TypeArgumentModel) signature).getAssociatedTypes();

        return types.size() > 0
                ? new SchemaProcessor(types.get(0), context).process()
                : anySchema();
    }

    private Schema<?> typeParameterSchema() {
        return ((TypeParameterModel) signature).getBounds().stream()
                .filter(Objects::nonNull)
                .filter(bound -> !bound.isNativeObject())
                .<Schema<?>> map(
                        bound -> new SchemaProcessor(bound, context)
                                .process())
                .filter(schema -> !Objects.equals(schema, anySchemaSample))
                .findFirst().orElseGet(this::anySchema);
    }

    private Schema<?> typeVariableSchema() {
        return new SchemaProcessor(((TypeVariableModel) signature).resolve(), context).process();
    }
}
