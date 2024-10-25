package com.vaadin.hilla.parser.plugins.nonnull.nullable.nonNullApi;

import com.vaadin.hilla.parser.plugins.nonnull.nullable.Endpoint;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Endpoint
public class NullableNonNullEndpoint {

    public NullableNonNullFieldModel nullableNonNullFieldModel(NullableNonNullFieldModel nullableNonNullFieldModel) {
        return nullableNonNullFieldModel;
    }

    public static class NullableNonNullFieldModel {
        public String required;
        @Id
        public String id;
        @Version
        public Long version;
        @Version
        @Nonnull
        public Long notNullVersion;
    }
}
