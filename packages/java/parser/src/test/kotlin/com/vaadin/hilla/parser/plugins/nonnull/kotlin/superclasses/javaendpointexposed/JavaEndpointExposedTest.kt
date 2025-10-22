package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed

import com.vaadin.hilla.parser.core.Parser
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.model.ModelPlugin
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig
import com.vaadin.hilla.parser.plugins.KotlinNullabilityPlugin
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.Endpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.helpers.TestHelper
import org.junit.jupiter.api.Test

class JavaEndpointExposedTest {
    private val helper: TestHelper = TestHelper(javaClass)

    @Test
    fun should_respectNullabilityAnnotations_of_methodsFromJavaEndpointExposed() {

        val nullabilityPlugin = NonnullPlugin()
        nullabilityPlugin.setConfiguration(
            NonnullPluginConfig(
                setOf(AnnotationMatcher(Nonnull::class.java.getName(), false, 0)),
            null)
        );

        val kotlinNullabilityPlugin = KotlinNullabilityPlugin()

        val openAPI = Parser()
            .classPath(setOf(helper.targetDir.toString()))
            .endpointAnnotations(listOf(Endpoint::class.java))
            .endpointExposedAnnotations(listOf(EndpointExposed::class.java))
            .addPlugin(BackbonePlugin())
            .addPlugin(ModelPlugin())
            .addPlugin(kotlinNullabilityPlugin)
            .addPlugin(nullabilityPlugin)
            .execute(listOf(PersonEndpoint::class.java))

        helper.executeParserWithConfig(openAPI)
    }
}
