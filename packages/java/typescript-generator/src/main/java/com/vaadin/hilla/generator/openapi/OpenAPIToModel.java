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
package com.vaadin.hilla.generator.openapi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.ParameterModel;
import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Builds the model the TypeScript generator works from out of an OpenAPI
 * definition.
 *
 * <p>
 * This is a temporary arrangement: the parser produces OpenAPI today, and this
 * lets the generator be written against the model it will eventually be given
 * directly. It goes away once the parser builds the model itself, and with it
 * the guesswork of reading back what the OpenAPI representation did not have a
 * place for.
 */
public final class OpenAPIToModel {
    private static final String MEDIA_TYPE = "application/json";
    private static final String OK = "200";
    private static final String REF_PREFIX = "#/components/schemas/";
    private static final String CLASS_NAME = "x-class-name";
    private static final String TYPE_ARGUMENTS = "x-type-arguments";
    private static final String TYPE_VARIABLE = "x-type-variable";

    private OpenAPIToModel() {
    }

    public static List<EndpointModel> endpoints(OpenAPI openAPI) {
        if (openAPI.getPaths() == null) {
            return List.of();
        }

        var classNames = classNames(openAPI);
        var methods = new LinkedHashMap<String, List<MethodModel>>();

        openAPI.getPaths().forEach((path, item) -> {
            var parts = path.split("/");

            if (parts.length != 3 || item.getPost() == null) {
                return;
            }

            methods.computeIfAbsent(parts[1], name -> new ArrayList<>())
                    .add(method(parts[2], item));
        });

        return methods.entrySet().stream()
                .map(entry -> new EndpointModel(entry.getKey(),
                        classNames.getOrDefault(entry.getKey(), entry.getKey()),
                        entry.getValue()))
                .toList();
    }

    /**
     * The Java class each endpoint comes from, which the parser keeps as a tag
     * of the definition.
     */
    private static Map<String, String> classNames(OpenAPI openAPI) {
        var names = new LinkedHashMap<String, String>();

        if (openAPI.getTags() != null) {
            openAPI.getTags().forEach(tag -> {
                var extensions = tag.getExtensions();

                if (extensions != null && extensions.get(CLASS_NAME) != null) {
                    names.put(tag.getName(),
                            String.valueOf(extensions.get(CLASS_NAME)));
                }
            });
        }

        return names;
    }

    private static MethodModel method(String name, PathItem item) {
        var operation = item.getPost();
        var parameters = new ArrayList<ParameterModel>();
        var requestBody = operation.getRequestBody();

        if (requestBody != null && requestBody.getContent() != null) {
            var schema = schemaOf(requestBody.getContent().get(MEDIA_TYPE));

            if (schema != null && schema.getProperties() != null) {
                schema.getProperties().forEach((parameter, type) -> parameters
                        .add(new ParameterModel(parameter, type(type))));
            }
        }

        return new MethodModel(name, parameters, returnType(operation));
    }

    private static TypeModel returnType(
            io.swagger.v3.oas.models.Operation operation) {
        if (operation.getResponses() == null) {
            return TypeModel.Scalar.of(TypeModel.ScalarKind.VOID);
        }

        var response = operation.getResponses().get(OK);

        if (response == null || response.getContent() == null) {
            return TypeModel.Scalar.of(TypeModel.ScalarKind.VOID);
        }

        var schema = schemaOf(response.getContent().get(MEDIA_TYPE));

        return schema == null ? TypeModel.Scalar.of(TypeModel.ScalarKind.VOID)
                : type(schema);
    }

    private static Schema<?> schemaOf(
            io.swagger.v3.oas.models.media.MediaType mediaType) {
        return mediaType == null ? null : mediaType.getSchema();
    }

    static TypeModel type(Schema<?> schema) {
        if (schema == null) {
            return TypeModel.Scalar.of(TypeModel.ScalarKind.UNKNOWN);
        }

        var typeVariable = extension(schema, TYPE_VARIABLE);

        if (typeVariable != null) {
            return new TypeModel.TypeVariable(String.valueOf(typeVariable));
        }

        var optional = Boolean.TRUE.equals(schema.getNullable());
        var unwrapped = unwrap(schema);

        if (unwrapped.get$ref() != null) {
            return new TypeModel.EntityRef(
                    unwrapped.get$ref().substring(REF_PREFIX.length()),
                    typeArguments(schema), optional);
        }

        return switch (unwrapped.getType() == null ? "" : unwrapped.getType()) {
        case "array" ->
            new TypeModel.ArrayOf(type(unwrapped.getItems()), optional);
        case "boolean" ->
            new TypeModel.Scalar(TypeModel.ScalarKind.BOOLEAN, optional);
        case "integer", "number" ->
            new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER, optional);
        case "string" ->
            new TypeModel.Scalar(TypeModel.ScalarKind.STRING, optional);
        case "object" -> object(unwrapped, optional);
        default -> new TypeModel.Scalar(TypeModel.ScalarKind.UNKNOWN, optional);
        };
    }

    /**
     * An object without properties and with additional ones is a map. Anything
     * else the generator has no better name for than unknown.
     */
    private static TypeModel object(Schema<?> schema, boolean optional) {
        if (schema.getProperties() != null || !(schema
                .getAdditionalProperties() instanceof Schema<?> values)) {
            return new TypeModel.Scalar(TypeModel.ScalarKind.UNKNOWN, optional);
        }

        return new TypeModel.MapOf(type(values), optional);
    }

    /**
     * A nullable reference is a composed schema wrapping the reference, so the
     * first member of it is what the type actually is.
     */
    private static Schema<?> unwrap(Schema<?> schema) {
        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            return schema.getAnyOf().get(0);
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            return schema.getAllOf().get(0);
        }

        return schema;
    }

    private static List<TypeModel> typeArguments(Schema<?> schema) {
        var arguments = extension(schema, TYPE_ARGUMENTS);

        if (arguments == null && schema.getAnyOf() != null) {
            arguments = schema.getAnyOf().stream()
                    .map(member -> extension(member, TYPE_ARGUMENTS))
                    .filter(java.util.Objects::nonNull).findFirst()
                    .orElse(null);
        }

        if (!(arguments instanceof Schema<?> composed)
                || composed.getAllOf() == null) {
            return List.of();
        }

        return composed.getAllOf().stream()
                .map(argument -> type((Schema<?>) argument)).toList();
    }

    private static Object extension(Schema<?> schema, String name) {
        return schema.getExtensions() == null ? null
                : schema.getExtensions().get(name);
    }
}
