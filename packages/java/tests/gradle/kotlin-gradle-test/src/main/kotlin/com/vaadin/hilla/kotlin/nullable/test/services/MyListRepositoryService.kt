package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.hilla.EndpointExposed
import com.vaadin.hilla.crud.filter.Filter
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

@EndpointExposed
class MyListRepositoryService<T, ID, R: CrudRepository<T, ID>> {

    fun list(pageable: Pageable, filter: Filter?): List<T> {
        return listOf()
    }

    fun count(filter: Filter?): Long {
        return 0
    }

    fun get(id: ID?): T? {
        return null
    }

    fun exists(id: ID): Boolean {
        return false
    }
}
