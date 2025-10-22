package com.vaadin.hilla.parser.plugins.backbone.enumtype;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

public class EnumTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateStringType_When_ReferringToEnumTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(EnumTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_When_ReferringToEnumTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(EnumTypeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("EnumTypeEndpoint.ts");
            assertNotNull(endpointTs, "EnumTypeEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type EnumEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/EnumEntity.js";
                import client_1 from "./connect-client.default.js";
                async function echoEnum_1(value: EnumEntity_1 | undefined, init?: EndpointRequestInit_1): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "echoEnum", { value }, init); }
                async function echoListEnum_1(enumList: Array<EnumEntity_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<EnumEntity_1 | undefined> | undefined> { return client_1.call("EnumTypeEndpoint", "echoListEnum", { enumList }, init); }
                async function getEnum_1(init?: EndpointRequestInit_1): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "getEnum", {}, init); }
                async function setEnum_1(value: EnumEntity_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("EnumTypeEndpoint", "setEnum", { value }, init); }
                export { echoEnum_1 as echoEnum, echoListEnum_1 as echoListEnum, getEnum_1 as getEnum, setEnum_1 as setEnum };
                """;
            assertTypeScriptEquals("EnumTypeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
