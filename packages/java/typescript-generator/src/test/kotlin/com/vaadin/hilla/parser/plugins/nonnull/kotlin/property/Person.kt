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
    // A second property without a field, declared after fullName: the JVM
    // lists methods in no particular order, so such properties are written
    // in the order of their names, which puts this one first
    fun getAlias() = firstName.lowercase()
    var luckyNumber by Delegates.notNull<Int>()
    lateinit var addresses: Map<String, Address>
    val profilePicture: String? by lazy {
        println("Computing data...")
        "Computed Data"
    }
}
