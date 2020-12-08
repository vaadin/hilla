package com.vaadin.flow.server.connect.generator.endpoints.superclassmethods;

import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.connect.generator.endpoints.superclassmethods.PersonEndpoint.Person;

@Endpoint
public class PersonEndpoint extends CrudEndpoint<Person, Integer>
        implements PagedData<Person> {

    public static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
