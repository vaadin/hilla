package com.vaadin.fusion.generator.endpoints.superclassmethods;

import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.generator.endpoints.superclassmethods.PersonEndpoint.Person;

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
