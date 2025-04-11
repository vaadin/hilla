package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

class Address(
    override var id: Long?,
    var street: String,
    var zipCode: String,
    var city: String?,
): AbstractEntity<Long?>()
