package dev.hilla;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class OpenAPIUtilTest {

    @Test
    public void emptySchemaReturnsNoComponents() throws IOException {
        Assert.assertEquals(Collections.emptySet(),
                parse("openapi-nocustomtypes.json"));
    }

    @Test
    public void singleType() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-customtype.json"));
    }

    @Test
    public void referringTypes() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.MyType",
                "com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-referring-customtypes.json"));
    }

    @Test
    public void nestedType() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.HelloReactEndpoint$MyInnerType"),
                parse("openapi-innertype.json"));
    }

    private Set<String> parse(String openapiFilename) throws IOException {
        String openApi = IOUtils.toString(
                getClass().getResourceAsStream(openapiFilename),
                StandardCharsets.UTF_8);
        return OpenAPIUtil.findOpenApiClasses(openApi);
    }

}
