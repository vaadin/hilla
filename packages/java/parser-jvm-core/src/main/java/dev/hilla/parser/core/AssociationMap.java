package dev.hilla.parser.core;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.SignatureModel;

import io.swagger.v3.oas.models.media.Schema;

public final class AssociationMap {
    private final Map<Schema<?>, ClassInfoModel> entities = new IdentityHashMap<>();
    private final Map<Schema<?>, FieldInfoModel> fields = new IdentityHashMap<>();
    private final Map<Schema<?>, MethodInfoModel> methods = new IdentityHashMap<>();
    private final Map<Schema<?>, MethodParameterInfoModel> parameters = new IdentityHashMap<>();
    private final Reversed reversed = new Reversed();
    private final Map<Schema<?>, SignatureModel> types = new IdentityHashMap<>();

    AssociationMap() {
    }

    public void addEntity(Schema<?> schema, ClassInfoModel entity) {
        entities.put(schema, entity);
        reversed.entities.put(entity, schema);
    }

    public void addField(Schema<?> schema, FieldInfoModel field) {
        fields.put(schema, field);
        reversed.fields.put(field, schema);
    }

    public void addMethod(Schema<?> schema, MethodInfoModel method) {
        methods.put(schema, method);
        reversed.methods.put(method, schema);
    }

    public void addParameter(Schema<?> schema,
            MethodParameterInfoModel parameter) {
        parameters.put(schema, parameter);
        reversed.parameters.put(parameter, schema);
    }

    public void addType(Schema<?> schema, SignatureModel signature) {
        types.put(schema, signature);
        reversed.types.put(signature, schema);
    }

    public Map<Schema<?>, ClassInfoModel> getEntities() {
        return Collections.unmodifiableMap(entities);
    }

    public Map<Schema<?>, FieldInfoModel> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public Map<Schema<?>, MethodInfoModel> getMethods() {
        return Collections.unmodifiableMap(methods);
    }

    public Map<Schema<?>, MethodParameterInfoModel> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public Map<Schema<?>, SignatureModel> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    public Reversed reversed() {
        return reversed;
    }

    public static class Reversed {
        private final Map<ClassInfoModel, Schema<?>> entities = new IdentityHashMap<>();
        private final Map<FieldInfoModel, Schema<?>> fields = new IdentityHashMap<>();
        private final Map<MethodInfoModel, Schema<?>> methods = new IdentityHashMap<>();
        private final Map<MethodParameterInfoModel, Schema<?>> parameters = new IdentityHashMap<>();
        private final Map<SignatureModel, Schema<?>> types = new IdentityHashMap<>();

        private Reversed() {
        }

        public Map<ClassInfoModel, Schema<?>> getEntities() {
            return Collections.unmodifiableMap(entities);
        }

        public Map<FieldInfoModel, Schema<?>> getFields() {
            return Collections.unmodifiableMap(fields);
        }

        public Map<MethodInfoModel, Schema<?>> getMethods() {
            return Collections.unmodifiableMap(methods);
        }

        public Map<MethodParameterInfoModel, Schema<?>> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        public Map<SignatureModel, Schema<?>> getTypes() {
            return Collections.unmodifiableMap(types);
        }
    }
}
