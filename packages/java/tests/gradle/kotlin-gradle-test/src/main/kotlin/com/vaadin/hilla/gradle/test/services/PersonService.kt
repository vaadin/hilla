package com.vaadin.hilla.gradle.test.services

import com.vaadin.hilla.gradle.test.data.Person
import com.vaadin.hilla.gradle.test.data.PersonRepository
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable
import com.vaadin.hilla.crud.CrudRepositoryService

@AnonymousAllowed
@BrowserCallable
class PersonService : CrudRepositoryService<Person, Long, PersonRepository>()
