package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import kotlin.properties.Delegates

open class Person(
    override var id: Long?,
    var firstName: @NotBlank String,
    var lastName: String,
    var email: @Email String?,
    var phone: String = "",
    private var dateOfBirth: LocalDate?,
    var important: Boolean,
): AbstractEntity<Long?>() {
    var age: Int?
        get() = dateOfBirth?.let { LocalDate.now().year - it.year } ?: 0
        set(value) {
            dateOfBirth = LocalDate.now().minusYears(value!!.toLong())
        }
    var luckyNumber by Delegates.notNull<Int>()
    lateinit var addresses: Map<String, Address>
    val profilePicture: String? by lazy {
        println("Computing data...")
        "Computed Data"
    }
}
