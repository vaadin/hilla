package com.vaadin.flow.connect;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

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
        @NonNull
        public String getDefaultName() {
            return "value";
        }

        @JsonProperty("customName")
        public void setDefaultName(@NonNull String defaultName) {
            this.defaultName = defaultName;
        }
    }
}
