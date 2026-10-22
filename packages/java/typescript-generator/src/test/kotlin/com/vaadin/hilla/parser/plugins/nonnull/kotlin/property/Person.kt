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
    // Backed by a field, so that fullName stays the only property here that
    // exists as getter and setter alone: the JVM does not keep the order of
    // methods, and two such properties could swap places in the output
    var age: Int? = null
        set(value) {
            field = value
            if (value != null) {
                dateOfBirth = LocalDate.now().minusYears(value.toLong())
            }
        }
    fun getFullName() = "$firstName $lastName"
    fun setFullName(fullName: String) {
        val parts = fullName.split(" ")
        firstName = parts[0]
        lastName = parts[1]
    }
    var luckyNumber by Delegates.notNull<Int>()
    lateinit var addresses: Map<String, Address>
    val profilePicture: String? by lazy {
        println("Computing data...")
        "Computed Data"
    }
}
