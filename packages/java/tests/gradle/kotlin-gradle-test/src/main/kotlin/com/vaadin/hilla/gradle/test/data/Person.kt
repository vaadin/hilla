package com.vaadin.hilla.gradle.test.data

import jakarta.persistence.Entity
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

@Entity
class Person(
    var firstName: @NotBlank String? = null,
    var lastName: String? = null,
    var email: @Email String? = null,
    var phone: String? = null,
    var dateOfBirth: LocalDate? = null,
    var occupation: String? = null,
    var role: String? = null,
    var important: Boolean = false,
): AbstractEntity()
