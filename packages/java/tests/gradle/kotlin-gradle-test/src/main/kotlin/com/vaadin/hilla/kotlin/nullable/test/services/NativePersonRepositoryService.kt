package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable
import com.vaadin.hilla.kotlin.nullable.test.data.NativePerson
import com.vaadin.hilla.kotlin.nullable.test.data.NativePersonRepository

@BrowserCallable
@AnonymousAllowed
class NativePersonRepositoryService: MyListRepositoryService<NativePerson, Long, NativePersonRepository>() {

    fun findByFirstName(firstName: String): List<NativePerson> {
        return listOf()
    }
}
