package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.util.Optional
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
        get() = dateOfBirth?.let { LocalDate.now().year - it.year }
        set(value) {
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
    // Declared as always there, and empty all the same when the server sends
    // nothing for it
    fun getNickname(): Optional<String> = Optional.ofNullable(null)
    var luckyNumber by Delegates.notNull<Int>()
    lateinit var addresses: Map<String, Address>
    val profilePicture: String? by lazy {
        println("Computing data...")
        "Computed Data"
    }
}
