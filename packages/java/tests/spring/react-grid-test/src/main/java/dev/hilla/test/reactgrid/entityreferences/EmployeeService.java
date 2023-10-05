package dev.hilla.test.reactgrid.entityreferences;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.ListRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class EmployeeService
        extends ListRepositoryService<Employee, Long, EmployeeRepository> {

}
