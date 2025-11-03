package com.vaadin.hilla.parser.plugins.backbone.jsonvalue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Endpoint
public class JsonValueEndpoint {
    public record Name(String firstName, String lastName) {
        @JsonCreator
        public Name(String fullName) {
            this(fullName.split(" ")[0], fullName.split(" ")[1]);
        }

        @JsonValue
        public String fullName() {
            return firstName + " " + lastName;
        }
    }

    public static class Email {
        private String value;

        @JsonCreator
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

    public record PhoneNumber(@JsonValue int value) {
        @JsonCreator
        public PhoneNumber {
        }
    }

    public record Person(Name name, Email email, PhoneNumber phoneNumber) {
    }

    public Person getPerson() {
        return new Person(new Name("John", "Doe"),
                new Email("john.doe@example.com"), new PhoneNumber(1234567890));
    }

    public void setPerson(Person person) {
    }

    public Email getEmail() {
        return new Email("john.doe@example.com");
    }

    public void setEmail(Email email) {
    }
}
