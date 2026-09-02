package com.vaadin.hilla.parser.plugins.nonnull.kotlin.property

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest
import org.junit.jupiter.api.Test

class MiscPropertiesTest : AbstractFullStackTest() {

    @Test
    fun should_computeKotlinNullability_forAllShapesOfProperties() {
        assertTypescriptMatchesSnapshot(PropertyEndpoint::class.java)
    }
}
