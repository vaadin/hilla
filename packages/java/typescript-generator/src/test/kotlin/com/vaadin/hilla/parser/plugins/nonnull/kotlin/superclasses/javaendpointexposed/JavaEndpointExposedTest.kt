package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed

import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig
import com.vaadin.hilla.parser.testutils.AbstractFullStackTest
import org.junit.jupiter.api.Test

class JavaEndpointExposedTest : AbstractFullStackTest() {

    @Test
    fun should_respectNullabilityAnnotations_of_methodsFromJavaEndpointExposed() {
        val plugin = NonnullPlugin()
        plugin.setConfiguration(
            NonnullPluginConfig(
                setOf(AnnotationMatcher(Nonnull::class.java.name, false, 0)),
                null
            )
        )

        assertTypescriptMatchesSnapshot(
            generator(PersonEndpoint::class.java).withPlugin(plugin)
        )
    }
}
