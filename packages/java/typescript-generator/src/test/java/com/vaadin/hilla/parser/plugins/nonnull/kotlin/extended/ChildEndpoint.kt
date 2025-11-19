package com.vaadin.hilla.parser.plugins.nonnull.kotlin.extended

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.Endpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic.SimpleEndpoint

@Endpoint
class ChildEndpoint : SimpleEndpoint() {

    fun childMethod1(name: String?, age: Int?): String {
        return if (name?.isBlank() == true) {
            "Hello stranger"
        } else {
            "Hello $name"
        }
    }
}
