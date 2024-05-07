package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

import com.fasterxml.jackson.annotation.JsonValue;

@Endpoint
public class JsonValueNoJsonCreatorEndpoint {
    public static class Email {
        private String value;

        public Email(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public Email getEmail() {
        return new Email("john.doe@example.com");
    }

    public void setEmail(Email email) {
    }
}
