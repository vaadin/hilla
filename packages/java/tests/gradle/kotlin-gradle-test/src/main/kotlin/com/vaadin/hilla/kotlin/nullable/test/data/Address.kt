package com.vaadin.hilla.kotlin.nullable.test.data

import jakarta.persistence.Entity

@Entity
class Address(
    var street: String,
    var zipCode: String,
    var city: String?,
): AbstractEntity()
