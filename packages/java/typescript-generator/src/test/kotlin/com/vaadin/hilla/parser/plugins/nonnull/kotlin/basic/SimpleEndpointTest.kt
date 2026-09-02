package com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest
import org.junit.jupiter.api.Test

class SimpleEndpointTest : AbstractFullStackTest() {

    @Test
    fun should_takeKotlinNullabilityIntoAccount() {
        assertTypescriptMatchesSnapshot(SimpleEndpoint::class.java)
    }
}
