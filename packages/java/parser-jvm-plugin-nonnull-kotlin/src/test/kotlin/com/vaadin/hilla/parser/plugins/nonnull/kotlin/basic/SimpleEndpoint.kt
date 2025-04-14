package com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.Endpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic.data.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate


@Endpoint
open class SimpleEndpoint {

    fun sayHello(name: String?, age: Int): String =
        if (name?.isBlank() == true) {
            "Hello stranger"
        } else {
            "Hello $name"
        }

    fun isValidPerson(person: Person): Boolean? {
        return person.firstName.isNotBlank() && person.dateOfBirth != null
    }

    fun getPersonsByFirstname(firstname: String): List<Person> {
        return listOf()
    }

    fun saveAllPersons(persons: List<Person>): List<Person?>? {
        return listOf()
    }

    fun saveDepartmentManager(data: Map<String, Manager?>): Map<String?, Manager>? {
        return mapOf()
    }

    fun saveTeam(team: Team<Employee, Manager?>?): Team<Employee, Manager?>? {
        return team
    }

    fun saveTeams(teams: List<Team<Employee?, Manager>?>?): List<Team<Employee?, Manager>?> {
        return listOf()
    }

    fun saveCompaniesPerCities(data: Map<String, Map<String, List<Map<String, List<Team<Employee, Manager?>>?>>>>): Map<String, Map<String, List<Map<String, List<Team<Employee?, Manager>?>>>>?> {
        return mapOf()
    }

    fun saveNestedPerson(person: NestedPerson): NestedPerson {
        return person
    }

    class NestedPerson(
        var firstName: @NotBlank String = "",
        var lastName: String = "",
        var email: @Email String? = null,
        var phone: String = "",
        var address: MutableList<Address> = mutableListOf(),
        var team: Map<String, List<Employee?>> = mapOf(),
        var dateOfBirth: LocalDate? = null,
        var occupation: String? = null,
        var role: String? = null,
        var important: Boolean = false,
    )
}
