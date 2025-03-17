package com.vaadin.hilla.kotlin.nullable.test.data

open class Employee: NativePerson()

open class Manager: Employee()

class Team<E, M>(var employees: List<E>, var managers: List<M>)
