package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class PersonService
        extends CrudRepositoryService<Person, Long, PersonRepository> {

}
