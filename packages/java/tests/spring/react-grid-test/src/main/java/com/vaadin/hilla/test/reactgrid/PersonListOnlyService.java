package com.vaadin.hilla.test.reactgrid;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class PersonListOnlyService
        extends ListRepositoryService<Person, Long, PersonRepository> {

}
