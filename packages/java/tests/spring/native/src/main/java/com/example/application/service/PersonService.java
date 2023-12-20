package com.example.application.service;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@AnonymousAllowed
public class PersonService
        extends CrudRepositoryService<Person, Long, PersonRepository> {
}
