package com.vaadin.hilla.kotlin.nullable.test.services

import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.hilla.BrowserCallable
import com.vaadin.hilla.kotlin.nullable.test.data.Employee
import com.vaadin.hilla.kotlin.nullable.test.data.Manager
import com.vaadin.hilla.kotlin.nullable.test.data.NativePerson
import com.vaadin.hilla.kotlin.nullable.test.data.Team

@BrowserCallable
@AnonymousAllowed
class NativeHelloService {

    fun sayHello(name: String?, age: Int): String =
        if (name?.isBlank() == true) {
            "Hello stranger"
        } else {
            "Hello $name"
        }

    fun isValidPerson(person: NativePerson): Boolean? {
        return person.firstName.isNotBlank()
    }

    fun getPersonsByFirstname(firstname: String): List<NativePerson> {
        return listOf()
    }

    fun saveAllPersons(persons: List<NativePerson>): List<NativePerson?>? {
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
}
