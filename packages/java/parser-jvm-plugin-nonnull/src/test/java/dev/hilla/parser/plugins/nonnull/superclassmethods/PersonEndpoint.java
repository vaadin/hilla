package dev.hilla.parser.plugins.nonnull.superclassmethods;

@Endpoint
public class PersonEndpoint extends CrudEndpoint<PersonEndpoint.Person, Integer>
        implements PagedData<PersonEndpoint.Person> {

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
