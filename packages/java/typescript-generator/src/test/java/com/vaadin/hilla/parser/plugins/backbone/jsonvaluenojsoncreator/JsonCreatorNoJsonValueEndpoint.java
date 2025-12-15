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
package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class JsonCreatorNoJsonValueEndpoint {

    public static class User {

        private final String name;
        private final int age;

        // Default constructor
        public User() {
            this.name = "Unknown";
            this.age = 0;
        }

        // Constructor used during deserialization
        @JsonCreator
        public User(@JsonProperty("name") String n,
                @JsonProperty("age") int a) {
            name = n;
            age = a;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    public User getUser() {
        return new User("John Doe", 42);
    }

    public void setUser(User user) {
    }
}
