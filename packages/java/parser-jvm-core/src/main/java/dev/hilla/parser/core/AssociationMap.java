package dev.hilla.parser.core;

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
    private final Map<Schema<?>, ClassInfoModel> entities = new IdentityHashMap<>();
    private final Map<Schema<?>, FieldInfoModel> fields = new IdentityHashMap<>();
    private final Map<Schema<?>, MethodInfoModel> methods = new IdentityHashMap<>();
    private final Map<Schema<?>, MethodParameterInfoModel> parameters = new IdentityHashMap<>();
    private final Reversed reversed = new Reversed();
    private final Map<Schema<?>, SignatureInfo> signatureInfo = new IdentityHashMap<>();
    private final Map<Schema<?>, SignatureModel> signatures = new IdentityHashMap<>();

    AssociationMap() {
    }

    public void addEntity(@Nonnull Schema<?> schema,
            @Nonnull ClassInfoModel entity) {
        entities.put(Objects.requireNonNull(schema),
                Objects.requireNonNull(entity));
        reversed.entities.put(entity, schema);
    }

    public void addField(@Nonnull Schema<?> schema,
            @Nonnull FieldInfoModel field) {
        fields.put(Objects.requireNonNull(schema),
                Objects.requireNonNull(field));
        reversed.fields.put(field, schema);
    }

    public void addMethod(@Nonnull Schema<?> schema,
            @Nonnull MethodInfoModel method) {
        methods.put(Objects.requireNonNull(schema),
                Objects.requireNonNull(method));
        reversed.methods.put(method, schema);
    }

    public void addParameter(@Nonnull Schema<?> schema,
            @Nonnull MethodParameterInfoModel parameter) {
        parameters.put(Objects.requireNonNull(schema),
                Objects.requireNonNull(parameter));
        reversed.parameters.put(parameter, schema);
    }

    public void addSignature(@Nonnull Schema<?> schema,
            @Nonnull SignatureModel signature) {
        addSignature(schema, signature, null);
    }

    public void addSignature(@Nonnull Schema<?> schema,
            @Nonnull SignatureModel signature, SignatureInfo info) {
        signatures.put(Objects.requireNonNull(schema),
                Objects.requireNonNull(signature));
        reversed.signatures.put(signature, schema);

        if (info != null) {
            signatureInfo.put(schema, info);
            reversed.signatureInfo.put(signature, info);
        }
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

    public Map<Schema<?>, SignatureInfo> getSignatureInfo() {
        return Collections.unmodifiableMap(signatureInfo);
    }

    public Map<Schema<?>, SignatureModel> getSignatures() {
        return Collections.unmodifiableMap(signatures);
    }

    public Reversed reversed() {
        return reversed;
    }

    public static class Reversed {
        private final Map<ClassInfoModel, Schema<?>> entities = new IdentityHashMap<>();
        private final Map<FieldInfoModel, Schema<?>> fields = new IdentityHashMap<>();
        private final Map<MethodInfoModel, Schema<?>> methods = new IdentityHashMap<>();
        private final Map<MethodParameterInfoModel, Schema<?>> parameters = new IdentityHashMap<>();
        private final Map<SignatureModel, SignatureInfo> signatureInfo = new IdentityHashMap<>();
        private final Map<SignatureModel, Schema<?>> signatures = new IdentityHashMap<>();

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

        public Map<SignatureModel, SignatureInfo> getSignatureInfo() {
            return Collections.unmodifiableMap(signatureInfo);
        }

        public Map<SignatureModel, Schema<?>> getSignatures() {
            return Collections.unmodifiableMap(signatures);
        }
    }

}
