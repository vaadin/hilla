package com.vaadin.hilla.parser.plugins.nonnull.kotlin.basic

import com.vaadin.hilla.parser.core.Parser
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.model.ModelPlugin
import com.vaadin.hilla.parser.plugins.KotlinNullabilityPlugin
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.Endpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.helpers.TestHelper
import org.junit.jupiter.api.Test

class SimpleEndpointTest {

    private val helper: TestHelper = TestHelper(javaClass)

    @Test
    fun should_takeKotlinNullabilityIntoAccount() {
        val plugin = KotlinNullabilityPlugin()

        val openAPI = Parser()
            .classPath(setOf(helper.targetDir.toString()))
            .endpointAnnotations(listOf(Endpoint::class.java))
            .endpointExposedAnnotations(listOf(EndpointExposed::class.java))
            .addPlugin(BackbonePlugin())
            .addPlugin(ModelPlugin())
            .addPlugin(plugin)
            .execute(listOf(SimpleEndpoint::class.java))

        helper.executeParserWithConfig(openAPI)
    }
}
