package dev.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.hilla.parser.models.ClassRefSignatureModel;
import dev.hilla.parser.models.SignatureModel;

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
    private final SignatureModel signature;

    public SchemaProcessor(SignatureModel signature) {
        this.signature = signature;
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
        } else if (signature.isDate()) {
            result = dateSchema();
        } else if (signature.isDateTime()) {
            result = dateTimeSchema();
        } else if (signature.isClassRef()) {
            result = refSchema();
        } else {
            result = anySchema();
        }

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        return nullify(new ArraySchema(), true);
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
        return nullify(new IntegerSchema(), !signature.isPrimitive()).format(
            signature.isLong() ? "int64" : "int32");
    }

    private Schema<?> iterableSchema() {
        return nullify(new ArraySchema(), true);
    }

    private Schema<?> mapSchema() {
        return nullify(new MapSchema(), true);
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !signature.isPrimitive()).format(
            signature.isFloat() ? "float" : "double");
    }

    private Schema<?> refSchema() {
        if (signature.isJDKClass()) {
            return anySchema();
        }

        var cls = ((ClassRefSignatureModel) signature).getClassInfo();

        return nullify(new ComposedSchema(), true).anyOf(new ArrayList<>(
            List.of(
                new Schema<>().$ref(COMPONENTS_SCHEMAS_REF + cls.getName()))));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !signature.isPrimitive());
    }
}
