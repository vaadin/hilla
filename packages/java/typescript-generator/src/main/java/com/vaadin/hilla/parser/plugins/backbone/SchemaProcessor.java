/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.plugins.backbone;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import java.util.ArrayList;
import java.util.List;

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

import com.vaadin.hilla.parser.models.ClassRefSignatureModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.models.TypeParameterModel;
import com.vaadin.hilla.parser.models.TypeVariableModel;

final class SchemaProcessor {
    private final SignatureModel type;
    private final boolean shouldHandleGenerics;

    /**
     * Creates a new configured instance.
     *
     * @param type
     *            the type to process
     * @param shouldHandleGenerics
     *            indicates if this processor must deal with generic type
     *            variables and parameters
     */
    public SchemaProcessor(SignatureModel type, boolean shouldHandleGenerics) {
        this.type = type;
        this.shouldHandleGenerics = shouldHandleGenerics;
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
        } else if (type.hasIntegerType() || type.isBigInteger()) {
            result = integerSchema();
        } else if (type.hasFloatType() || type.isBigDecimal()) {
            result = numberSchema();
        } else if (type.isArray()) {
            result = arraySchema();
        } else if (type.isIterable()) {
            result = iterableSchema();
        } else if (type.isMap()) {
            result = mapSchema();
        } else if (type.isDate()) {
            result = dateSchema();
        } else if (type.isDateTime()) {
            result = dateTimeSchema();
        } else if (type.isClassRef()) {
            result = refSchema();
        } else if (shouldHandleGenerics && type.isTypeVariable()) {
            result = typeVariableSchema();
        } else if (shouldHandleGenerics && type.isTypeParameter()) {
            result = typeParameterSchema();
        } else {
            result = anySchema();
        }

        return result;
    }

    private Schema<?> anySchema() {
        return new ObjectSchema();
    }

    private Schema<?> arraySchema() {
        // For bare arrays without type information, set items to anySchema
        // to ensure valid OpenAPI (items is required for ArraySchema)
        return nullify(new ArraySchema().items(anySchema()), true);
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
        return nullify(new IntegerSchema(), !type.isPrimitive()).format(
                type.isLong() || type.isBigInteger() ? "int64" : "int32");
    }

    private Schema<?> iterableSchema() {
        // For bare iterables without type information, set items to anySchema
        // to ensure valid OpenAPI (items is required for ArraySchema)
        var schema = nullify(new ArraySchema().items(anySchema()), true);
        var _type = (ClassRefSignatureModel) type;

        if (type.isNonJDKClass()) {
            schema.addExtension("x-class-name", _type.getName());
        }

        return schema;
    }

    private Schema<?> mapSchema() {
        var _type = (ClassRefSignatureModel) type;

        // For the TS generator, to recognize a schema as a map, it requires
        // "additionalProperties" to be set. The default "any" option could be
        // updated later.
        var schema = nullify(new MapSchema(), true)
                .additionalProperties(anySchema());

        if (type.isNonJDKClass()) {
            schema.addExtension("x-class-name", _type.getName());
        }

        return schema;
    }

    private Schema<?> numberSchema() {
        return nullify(new NumberSchema(), !type.isPrimitive())
                .format(type.isFloat() ? "float" : "double");
    }

    private Schema<?> refSchema() {
        if (type.isJDKClass()) {
            return anySchema();
        }

        var _type = (ClassRefSignatureModel) type;
        var fullyQualifiedName = _type.getClassInfo().getName();

        return nullify(new ComposedSchema(), true)
                .anyOf(new ArrayList<>(List.of(new Schema<>()
                        .$ref(COMPONENTS_SCHEMAS_REF + fullyQualifiedName))));
    }

    private Schema<?> stringSchema() {
        return nullify(new StringSchema(), !type.isPrimitive());
    }

    private Schema<?> typeVariableSchema() {
        var typeVarModel = (TypeVariableModel) type;
        var schema = nullify(new ObjectSchema(), true);
        schema.addExtension("x-type-variable", typeVarModel.getName());
        return schema;
    }

    private Schema<?> typeParameterSchema() {
        var typeParamModel = (TypeParameterModel) type;
        var schema = nullify(new ObjectSchema(), true);
        schema.addExtension("x-type-variable", typeParamModel.getName());
        return schema;
    }
}
