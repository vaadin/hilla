package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.hilla.Endpoint

@Endpoint
class ChildEndpoint : ParentEndpoint() {

    fun childMethod1(name: String?, age: Int?): String {
        return if (name?.isBlank() == true) {
            "Hello stranger"
        } else {
            "Hello $name"
        }
    }
}
