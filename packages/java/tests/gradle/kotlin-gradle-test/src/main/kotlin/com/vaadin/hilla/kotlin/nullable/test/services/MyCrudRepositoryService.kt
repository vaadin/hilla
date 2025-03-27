package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.hilla.EndpointExposed
import com.vaadin.hilla.gradle.test.data.Person
import org.springframework.data.repository.CrudRepository

@EndpointExposed
class MyCrudRepositoryService<T, ID, R: CrudRepository<T, ID>> : MyListRepositoryService<T, ID, R>() {

    fun update(entity: T?): T? {
        return entity
    }

    fun delete(id: ID?) = Unit

}
