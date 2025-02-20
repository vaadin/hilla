package com.vaadin.hilla.gradle.test.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor


interface PersonRepository : JpaRepository<Person, Long>, JpaSpecificationExecutor<Person>
