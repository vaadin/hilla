package com.vaadin.hilla.kotlin.nullable.test.data

class Employee : NativePerson()

class Manager : Employee()

class Team<E, M>(var employees: List<Employee>?, var manager: Manager?): AbstractEntity()
