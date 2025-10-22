package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.endpointexposed

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed

@EndpointExposed
interface PagedData<T> {
    fun getNonNullablePage(
        pageSize: Int, pageNumber: Int,
        parameters: Map<String, T>
    ): List<T> {
        return emptyList()
    }

    fun getPage(pageSize: Int, pageNumber: Int): List<T> {
        return emptyList()
    }

    fun size(): Int {
        return 0
    }
}
