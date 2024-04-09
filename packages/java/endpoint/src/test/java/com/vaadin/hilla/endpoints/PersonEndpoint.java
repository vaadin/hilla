package com.vaadin.hilla.endpoints;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.endpoints.PersonEndpoint.Person;

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
