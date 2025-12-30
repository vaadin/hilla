package com.vaadin.hilla.parser.plugins.nonnull.kotlin.extended

import com.vaadin.hilla.parser.core.Parser
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.model.ModelPlugin
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.KotlinNullabilityPlugin
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.helpers.TestHelper
import com.vaadin.hilla.parser.testutils.annotations.Endpoint
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed
import org.junit.jupiter.api.Test

class ChildEndpointTest {

    private val helper: TestHelper = TestHelper(javaClass)

    @Test
    fun should_takeKotlinNullabilityIntoAccount_of_extended_methods() {
        val plugin = KotlinNullabilityPlugin()

        val openAPI = Parser()
            .classPath(setOf(helper.targetDir.toString()))
            .endpointAnnotations(listOf(Endpoint::class.java))
            .endpointExposedAnnotations(listOf(EndpointExposed::class.java))
            .addPlugin(BackbonePlugin())
            .addPlugin(ModelPlugin())
            .addPlugin(plugin)
            .execute(listOf(ChildEndpoint::class.java))

        helper.executeParserWithConfig(openAPI)
    }
}
