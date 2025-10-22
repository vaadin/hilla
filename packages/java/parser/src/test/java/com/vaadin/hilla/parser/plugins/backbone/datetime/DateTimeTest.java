package com.vaadin.hilla.parser.plugins.backbone.datetime;

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

public class DateTimeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateStringType_When_ReferringToDateTimeTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(DateTimeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_When_ReferringToDateTimeTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(DateTimeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("DateTimeEndpoint.ts");
            assertNotNull(endpointTs, "DateTimeEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function echoCustomDate_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoCustomDate", {}, init); }
                async function echoDate_1(date: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoDate", { date }, init); }
                async function echoInstant_1(instant: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoInstant", { instant }, init); }
                async function echoListLocalDateTime_1(localDateTimeList: Array<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("DateTimeEndpoint", "echoListLocalDateTime", { localDateTimeList }, init); }
                async function echoLocalDate_1(localDate: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoLocalDate", { localDate }, init); }
                async function echoLocalDateTime_1(localDateTime: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoLocalDateTime", { localDateTime }, init); }
                async function echoLocalTime_1(localTime: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoLocalTime", { localTime }, init); }
                async function echoMapInstant_1(mapInstant: Record<string, string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Record<string, string | undefined> | undefined> { return client_1.call("DateTimeEndpoint", "echoMapInstant", { mapInstant }, init); }
                async function echoOffsetDateTime_1(offsetDateTime: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoOffsetDateTime", { offsetDateTime }, init); }
                async function echoZonedDateTime_1(zonedDateTime: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("DateTimeEndpoint", "echoZonedDateTime", { zonedDateTime }, init); }
                export { echoCustomDate_1 as echoCustomDate, echoDate_1 as echoDate, echoInstant_1 as echoInstant, echoListLocalDateTime_1 as echoListLocalDateTime, echoLocalDate_1 as echoLocalDate, echoLocalDateTime_1 as echoLocalDateTime, echoLocalTime_1 as echoLocalTime, echoMapInstant_1 as echoMapInstant, echoOffsetDateTime_1 as echoOffsetDateTime, echoZonedDateTime_1 as echoZonedDateTime };
                """;
            assertTypeScriptEquals("DateTimeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
