package com.vaadin.hilla.parser.plugins.backbone.jsonvalueunbalanced;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
