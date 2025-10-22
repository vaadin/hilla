package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.endpointexposed

import com.vaadin.hilla.parser.core.Parser
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.model.ModelPlugin
import com.vaadin.hilla.parser.plugins.KotlinNullabilityPlugin
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.Endpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.helpers.TestHelper
import org.junit.jupiter.api.Test

class EndpointExposedTest {
    private val helper: TestHelper = TestHelper(javaClass)

    @Test
    fun should_correctlyResolveNullability_of_methodsFromHierarchyOfEndpointExposed() {

        val kotlinNullabilityPlugin = KotlinNullabilityPlugin()

        val openAPI = Parser()
            .classPath(setOf(helper.targetDir.toString()))
            .endpointAnnotations(listOf(Endpoint::class.java))
            .endpointExposedAnnotations(listOf(EndpointExposed::class.java))
            .addPlugin(BackbonePlugin())
            .addPlugin(ModelPlugin())
            .addPlugin(kotlinNullabilityPlugin)
            .execute(listOf(PersonEndpoint::class.java))

        helper.executeParserWithConfig(openAPI)
    }
}
