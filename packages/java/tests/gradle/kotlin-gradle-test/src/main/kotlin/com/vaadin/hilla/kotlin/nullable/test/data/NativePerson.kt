package com.vaadin.hilla.kotlin.nullable.test.data

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.Email
import java.time.LocalDate

@Entity
open class NativePerson(
    var firstName: String? = null,
    var lastName: String = "",
    var email: @Email String? = null,
    var phone: String = "",
    @OneToMany
    var address: List<Address>? = null,
    var dateOfBirth: LocalDate? = null,
    var occupation: String? = null,
    var role: String? = null,
    var important: Boolean = false,
): AbstractEntity()
