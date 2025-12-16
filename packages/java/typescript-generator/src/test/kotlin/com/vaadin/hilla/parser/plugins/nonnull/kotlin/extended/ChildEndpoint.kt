package com.vaadin.hilla.parser.plugins.nonnull.kotlin.extended

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic.SimpleEndpoint
import com.vaadin.hilla.parser.testutils.annotations.Endpoint

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
