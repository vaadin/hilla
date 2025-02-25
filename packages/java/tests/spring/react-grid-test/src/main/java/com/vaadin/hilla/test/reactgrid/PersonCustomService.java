package com.vaadin.hilla.test.reactgrid;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.JpaFilterConverter;
import com.vaadin.hilla.crud.filter.OrFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter.Matcher;

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

    public List<Person> listPersonsLazyWithFilter(Pageable pageable,
            String filterString) {
        OrFilter filter = new OrFilter();
        PropertyStringFilter firstName = new PropertyStringFilter();
        firstName.setFilterValue(filterString);
        firstName.setPropertyId("firstName");
        firstName.setMatcher(Matcher.CONTAINS);
        PropertyStringFilter lastName = new PropertyStringFilter();
        lastName.setFilterValue(filterString);
        lastName.setPropertyId("lastName");
        lastName.setMatcher(Matcher.CONTAINS);
        filter.setChildren(List.of(firstName, lastName));
        return repository
                .findAll(JpaFilterConverter.toSpec(filter, Person.class),
                        pageable)
                .getContent();
    }
}
