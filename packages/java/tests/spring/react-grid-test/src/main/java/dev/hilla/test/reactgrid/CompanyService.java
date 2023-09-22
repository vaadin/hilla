package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class CompanyService extends CrudRepositoryService<Company, Long> {

    public CompanyService(CompanyRepository repository) {
        super(Company.class, repository);
    }
}
