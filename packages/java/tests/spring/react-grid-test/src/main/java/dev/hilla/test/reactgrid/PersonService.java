package dev.hilla.test.reactgrid;

import java.util.List;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class PersonService extends CrudRepositoryService<Person, Long> {

    public PersonService(PersonRepository repository) {
        super(repository);
    }

    @Override
    public List<Person> list(Pageable pageable) {
        // Workaround for https://github.com/vaadin/hilla/issues/1250
        return super.list(pageable);
    }
}
