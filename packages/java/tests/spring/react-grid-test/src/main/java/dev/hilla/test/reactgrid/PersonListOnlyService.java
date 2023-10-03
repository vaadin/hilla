package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.ListRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class PersonListOnlyService
        extends ListRepositoryService<Person, Long, PersonRepository> {

}
