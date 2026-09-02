package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.endpointexposed

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest
import org.junit.jupiter.api.Test

class EndpointExposedTest : AbstractFullStackTest() {

    @Test
    fun should_correctlyResolveNullability_of_methodsFromHierarchyOfEndpointExposed() {
        assertTypescriptMatchesSnapshot(PersonEndpoint::class.java)
    }
}
