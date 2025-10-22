package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

abstract class AbstractEntity<ID>(
    val version: Int = 0,
) {
    abstract var id: ID

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is AbstractEntity<*>) return false
        if (other === this) return true
        return id == other.id
    }
}
