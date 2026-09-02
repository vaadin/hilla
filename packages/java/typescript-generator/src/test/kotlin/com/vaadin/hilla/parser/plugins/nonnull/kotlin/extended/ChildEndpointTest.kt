package com.vaadin.hilla.parser.plugins.nonnull.kotlin.extended

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest
import org.junit.jupiter.api.Test

class ChildEndpointTest : AbstractFullStackTest() {

    @Test
    fun should_takeKotlinNullabilityIntoAccount_of_extended_methods() {
        assertTypescriptMatchesSnapshot(ChildEndpoint::class.java)
    }
}
