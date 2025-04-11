package com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic.data

import jakarta.persistence.Entity

@Entity
class Address(
    var street: String,
    var zipCode: String,
    var city: String?,
): AbstractEntity()
