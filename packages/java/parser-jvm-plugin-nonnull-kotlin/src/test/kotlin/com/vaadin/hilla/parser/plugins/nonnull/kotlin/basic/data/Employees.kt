package com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic.data

open class Employee : Person()

class Manager : Employee()

class Team<E, M>(var employees: List<Employee>?, var manager: Manager?): AbstractEntity()
