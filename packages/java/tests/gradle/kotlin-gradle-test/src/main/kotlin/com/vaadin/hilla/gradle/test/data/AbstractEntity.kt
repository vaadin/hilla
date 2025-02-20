package com.vaadin.hilla.gradle.test.data

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Version

@MappedSuperclass
abstract class AbstractEntity(
    @Version
    val version: Int = 0,
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "idgenerator"
    ) // The initial value is to account for data.sql demo data ids
    @SequenceGenerator(name = "idgenerator", initialValue = 1000)
    var id: Long? = null,
) {
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is AbstractEntity) {
            return false
        }
        return id == other.id
    }
}
