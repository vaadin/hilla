package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable
import com.vaadin.hilla.crud.CrudRepositoryService
import com.vaadin.hilla.kotlin.nullable.test.data.NativePerson
import com.vaadin.hilla.kotlin.nullable.test.data.NativePersonRepository

@BrowserCallable
@AnonymousAllowed
class NativePersonService: CrudRepositoryService<NativePerson, Long, NativePersonRepository>() {

}
