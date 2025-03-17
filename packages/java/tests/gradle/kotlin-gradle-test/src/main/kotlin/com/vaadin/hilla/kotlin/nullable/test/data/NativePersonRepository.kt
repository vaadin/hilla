package com.vaadin.hilla.kotlin.nullable.test.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface NativePersonRepository : JpaRepository<NativePerson, Long>, JpaSpecificationExecutor<NativePerson>
