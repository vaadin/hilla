package dev.hilla.springnative;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class HillaHintsRegistrarTest {

    @Test
    public void emptySchemaReturnsNoComponents() throws IOException {
        Assert.assertEquals(Collections.emptyList(),
                parse("openapi-nocustomtypes.json"));
    }

    @Test
    public void singleType() throws IOException {
        Assert.assertEquals(List
                .of("com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-customtype.json"));
    }

    @Test
    public void referringTypes() throws IOException {
        Assert.assertEquals(List.of(
                "com.example.application.endpoints.helloreact.MyType",
                "com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-referring-customtypes.json"));
    }

    @Test
    public void nestedType() throws IOException {
        Assert.assertEquals(List.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint$MyInnerType"),
                parse("openapi-innertype.json"));
    }

    private List<String> parse(String openapiFilename) throws IOException {
        String openApi = IOUtils.toString(
                getClass().getResourceAsStream(openapiFilename),
                StandardCharsets.UTF_8);
        return HillaHintsRegistrar.parseOpenApi(openApi);
    }

}
