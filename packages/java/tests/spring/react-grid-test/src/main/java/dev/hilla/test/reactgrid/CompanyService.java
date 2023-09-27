package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class CompanyService
        extends CrudRepositoryService<Company, Long, CompanyRepository> {
}
