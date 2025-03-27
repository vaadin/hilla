package com.vaadin.hilla.kotlin.nullable.test.data

import jakarta.persistence.*
import java.util.concurrent.atomic.AtomicReference

@MappedSuperclass
abstract class AbstractEntity<ID>(
    @Version
    val version: Int = 0,
) {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "idgenerator"
    )
    @SequenceGenerator(name = "idgenerator", initialValue = 1000)
    var id: AtomicReference<ID> = AtomicReference()

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is AbstractEntity<*>) return false
        return id == other.id
    }
}
