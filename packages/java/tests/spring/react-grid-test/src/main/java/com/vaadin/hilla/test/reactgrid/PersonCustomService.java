/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
