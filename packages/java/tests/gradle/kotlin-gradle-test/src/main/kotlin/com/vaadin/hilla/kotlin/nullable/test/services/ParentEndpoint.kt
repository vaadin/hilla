package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.hilla.Endpoint

@Endpoint
class ParentEndpoint {

    fun parentMethod1(name: String, age: Int?): String {
        return "Hello $name"
    }
}
