package com.vaadin.flow.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import jakarta.validation.constraints.NotBlank;

import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Endpoint to verify Jackson annotations support.
 */
@Endpoint
@AnonymousAllowed
public class AnnotatedEndpoint {
    public AnnotatedEntity getAnnotatedEntity() {
        return new AnnotatedEntity();
    }

    /**
     * Bean annotated with Jackson annotations.
     */
    public static class AnnotatedEntity {
        @NotBlank
        private String defaultName = "value";

        @JsonProperty("customName")
        @Nonnull
        public String getDefaultName() {
            return "value";
        }

        @JsonProperty("customName")
        public void setDefaultName(@Nonnull String defaultName) {
            this.defaultName = defaultName;
        }
    }
}
