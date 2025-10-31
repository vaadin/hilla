package com.vaadin.hilla.parser.plugins.nonnull.nullable;

import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Endpoint
public class NullableEndpoint {

    public NullableFieldModel nullableFieldModel(
            NullableFieldModel nullableFieldModel) {
        return nullableFieldModel;
    }

    public static class NullableFieldModel {
        @Id
        public String id;
        @Version
        public Long version;

        @jakarta.annotation.Nonnull
        public String jakartaNonnull;

        @org.jspecify.annotations.NonNull
        public String jspecifyNonnull;

        @org.springframework.lang.NonNull
        public String springNonnull;
    }
}
