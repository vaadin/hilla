package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

import java.util.Optional

class Address(
    override var id: Long?,
    var street: String,
    var zipCode: String,
    var city: String?,
): AbstractEntity<Long?>() {
    // Declared as always there, and empty all the same when the server sends
    // nothing for it. The only property written as a getter here: the JVM
    // does not keep the order of methods, so two of them could swap places.
    fun getLabel(): Optional<String> = Optional.empty()
}
