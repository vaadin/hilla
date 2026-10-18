package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

import java.util.Optional

class Address(
    override var id: Long?,
    var street: String,
    var zipCode: String,
    var city: String?,
): AbstractEntity<Long?>() {
    // Declared as always there, and empty all the same when the server sends
    // nothing for it
    fun getLabel(): Optional<String> = Optional.empty()
}
