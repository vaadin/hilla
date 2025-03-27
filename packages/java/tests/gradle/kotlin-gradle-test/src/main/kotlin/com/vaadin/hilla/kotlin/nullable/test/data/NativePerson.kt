package com.vaadin.hilla.kotlin.nullable.test.data

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Transient
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

@Entity
class NativePerson(
    var firstName: @NotBlank String = "",
    var lastName: String = "",
    var email: @Email String? = null,
    var phone: String = "",
    @OneToMany
    var address: MutableList<Address> = mutableListOf(),
    @Transient
    var team: Map<String, List<Employee?>> = mapOf(),
    private var dateOfBirth: LocalDate? = null,
    var occupation: String? = null,
    var role: String? = null,
    var important: Boolean = false,
): AbstractEntity<Long?>() {
    var age: Int?
        get() = dateOfBirth?.let { LocalDate.now().year - it.year } ?: 0
        set(value) {
            dateOfBirth = LocalDate.now().minusYears(value!!.toLong())
        }
    lateinit var data: String
}
