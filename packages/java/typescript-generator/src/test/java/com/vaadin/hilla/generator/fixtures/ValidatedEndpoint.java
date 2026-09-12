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
package com.vaadin.hilla.generator.fixtures;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Takes a type whose values are constrained by the annotations of the
 * validation API, and one of which says what it is to the persistence one.
 */
@Endpoint
public class ValidatedEndpoint {
    public void save(Validated entity) {
    }

    public static class Validated {
        @Id
        private long id;

        @NotBlank
        private String name;

        @Min(1)
        private int count;

        @Email(message = "not an address")
        private String address;

        @Size(min = 1, max = 10)
        private List<@NotBlank String> tags;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public String getAddress() {
            return address;
        }

        public List<String> getTags() {
            return tags;
        }
    }
}
