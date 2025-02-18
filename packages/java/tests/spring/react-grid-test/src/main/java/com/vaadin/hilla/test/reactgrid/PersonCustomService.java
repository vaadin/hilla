package com.vaadin.hilla.test.reactgrid;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@Service
@BrowserCallable
@AnonymousAllowed
public class PersonCustomService {

    private PersonRepository repository;

    PersonCustomService(PersonRepository repository) {
        this.repository = repository;
    }

    public List<Person> listPersonsEager() {
        return repository.findAll();
    }

    public List<Person> listPersonsLazy(Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }
}
