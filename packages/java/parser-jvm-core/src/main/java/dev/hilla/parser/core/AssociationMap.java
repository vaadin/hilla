package dev.hilla.parser.core;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import io.swagger.v3.oas.models.media.Schema;

public final class AssociationMap {
    private final Map<Schema<?>, RelativeClassInfo> entities = new IdentityHashMap<>();
    private final Map<Schema<?>, RelativeFieldInfo> fields = new IdentityHashMap<>();
    private final Map<Schema<?>, RelativeMethodInfo> methods = new IdentityHashMap<>();
    private final Map<Schema<?>, RelativeMethodParameterInfo> parameters = new IdentityHashMap<>();
    private final Reversed reversed = new Reversed();
    private final Map<Schema<?>, RelativeTypeSignature> types = new IdentityHashMap<>();

    AssociationMap() {
    }

    public void addEntity(Schema<?> schema, RelativeClassInfo entity) {
        entities.put(schema, entity);
        reversed.entities.put(entity, schema);
    }

    public void addField(Schema<?> schema, RelativeFieldInfo field) {
        fields.put(schema, field);
        reversed.fields.put(field, schema);
    }

    public void addMethod(Schema<?> schema, RelativeMethodInfo method) {
        methods.put(schema, method);
        reversed.methods.put(method, schema);
    }

    public void addParameter(Schema<?> schema,
            RelativeMethodParameterInfo parameter) {
        parameters.put(schema, parameter);
        reversed.parameters.put(parameter, schema);
    }

    public void addType(Schema<?> schema, RelativeTypeSignature signature) {
        types.put(schema, signature);
        reversed.types.put(signature, schema);
    }

    public Map<Schema<?>, RelativeClassInfo> getEntities() {
        return Collections.unmodifiableMap(entities);
    }

    public Map<Schema<?>, RelativeFieldInfo> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public Map<Schema<?>, RelativeMethodInfo> getMethods() {
        return Collections.unmodifiableMap(methods);
    }

    public Map<Schema<?>, RelativeMethodParameterInfo> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public Map<Schema<?>, RelativeTypeSignature> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    public Reversed reversed() {
        return reversed;
    }

    public static class Reversed {
        private final Map<RelativeClassInfo, Schema<?>> entities = new IdentityHashMap<>();
        private final Map<RelativeFieldInfo, Schema<?>> fields = new IdentityHashMap<>();
        private final Map<RelativeMethodInfo, Schema<?>> methods = new IdentityHashMap<>();
        private final Map<RelativeMethodParameterInfo, Schema<?>> parameters = new IdentityHashMap<>();
        private final Map<RelativeTypeSignature, Schema<?>> types = new IdentityHashMap<>();

        private Reversed() {
        }

        public Map<RelativeClassInfo, Schema<?>> getEntities() {
            return Collections.unmodifiableMap(entities);
        }

        public Map<RelativeFieldInfo, Schema<?>> getFields() {
            return Collections.unmodifiableMap(fields);
        }

        public Map<RelativeMethodInfo, Schema<?>> getMethods() {
            return Collections.unmodifiableMap(methods);
        }

        public Map<RelativeMethodParameterInfo, Schema<?>> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        public Map<RelativeTypeSignature, Schema<?>> getTypes() {
            return Collections.unmodifiableMap(types);
        }
    }
}
