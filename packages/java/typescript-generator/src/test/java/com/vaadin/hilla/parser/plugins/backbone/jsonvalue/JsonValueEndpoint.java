/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.plugins.backbone.jsonvalue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

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
