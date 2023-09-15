package dev.hilla.test.reactgrid;

import java.util.List;

import dev.hilla.BrowserCallable;
import dev.hilla.Nullable;
import dev.hilla.crud.CrudRepositoryService;
import dev.hilla.crud.filter.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class PersonService extends CrudRepositoryService<Person, Long> {

    public PersonService(PersonRepository repository) {
        super(Person.class, repository);
    }

    @Override
    public List<Person> list(Pageable pageable, @Nullable Filter filter) {
        // Workaround for https://github.com/vaadin/hilla/issues/1250
        return super.list(pageable, filter);
    }
}
