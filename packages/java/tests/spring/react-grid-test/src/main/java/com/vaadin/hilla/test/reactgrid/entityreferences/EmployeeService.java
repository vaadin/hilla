package com.vaadin.hilla.test.reactgrid.entityreferences;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class EmployeeService
        extends ListRepositoryService<Employee, Long, EmployeeRepository> {

}
