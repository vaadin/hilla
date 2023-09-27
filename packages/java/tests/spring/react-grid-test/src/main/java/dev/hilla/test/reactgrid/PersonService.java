package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class PersonService extends CrudRepositoryService<Person, Long> {

    public PersonService(PersonRepository repository) {
        super(Person.class, repository);
    }
}
