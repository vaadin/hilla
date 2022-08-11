package dev.hilla.parser.plugins.backbone;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.SignatureModel;

import io.swagger.v3.oas.models.media.Schema;

public final class AssociationMap {
    private final Map<ClassInfoModel, Schema<?>> entities = new IdentityHashMap<>();
    private final Map<FieldInfoModel, Schema<?>> fields = new IdentityHashMap<>();
    private final Map<MethodInfoModel, Schema<?>> methods = new IdentityHashMap<>();
    private final Map<MethodParameterInfoModel, Schema<?>> parameters = new IdentityHashMap<>();
    private final Reversed reversed = new Reversed();
    private final Map<SignatureModel, Schema<?>> signatures = new IdentityHashMap<>();

    AssociationMap() {
    }

    public void addEntity(@Nonnull ClassInfoModel entity,
            @Nonnull Schema<?> schema) {
        entities.put(Objects.requireNonNull(entity),
                Objects.requireNonNull(schema));
        reversed.entities.put(schema, entity);
    }

    public void addField(@Nonnull FieldInfoModel field,
            @Nonnull Schema<?> schema) {
        fields.put(Objects.requireNonNull(field),
                Objects.requireNonNull(schema));
        reversed.fields.put(schema, field);
    }

    public void addMethod(@Nonnull MethodInfoModel method,
            @Nonnull Schema<?> schema) {
        methods.put(Objects.requireNonNull(method),
                Objects.requireNonNull(schema));
        reversed.methods.put(schema, method);
    }

    public void addParameter(@Nonnull MethodParameterInfoModel parameter,
            @Nonnull Schema<?> schema) {
        parameters.put(Objects.requireNonNull(parameter),
                Objects.requireNonNull(schema));
        reversed.parameters.put(schema, parameter);
    }

    public void addSignature(@Nonnull SignatureModel signature,
            @Nonnull Schema<?> schema) {
        signatures.put(Objects.requireNonNull(signature),
                Objects.requireNonNull(schema));
        reversed.signatures.put(schema, signature);
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

    public Map<SignatureModel, Schema<?>> getSignatures() {
        return Collections.unmodifiableMap(signatures);
    }

    public Reversed reversed() {
        return reversed;
    }

    public static class Reversed {
        private final Map<Schema<?>, ClassInfoModel> entities = new IdentityHashMap<>();
        private final Map<Schema<?>, FieldInfoModel> fields = new IdentityHashMap<>();
        private final Map<Schema<?>, MethodInfoModel> methods = new IdentityHashMap<>();
        private final Map<Schema<?>, MethodParameterInfoModel> parameters = new IdentityHashMap<>();
        private final Map<Schema<?>, SignatureModel> signatures = new IdentityHashMap<>();

        private Reversed() {
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

        public Map<Schema<?>, SignatureModel> getSignatures() {
            return Collections.unmodifiableMap(signatures);
        }
    }

}
