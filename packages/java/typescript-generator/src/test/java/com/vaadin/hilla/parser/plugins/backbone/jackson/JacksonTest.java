package com.vaadin.hilla.parser.plugins.backbone.jackson;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.jackson.JacksonEndpoint.Sample;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class JacksonTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyIgnoreFieldsBasedOnJSONAnnotations()
            throws IOException, URISyntaxException {
        // This test is only run on JDKs that return fields and methods in the
        // order they are defined. Some JDKs like JetBrains Runtime does not
        Assumptions.assumeTrue(fieldsReturnedInDefinedOrder(),
                "This test is skipped on JDKs that do not return declared methods in the file order");

        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(JacksonEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    private boolean fieldsReturnedInDefinedOrder() {
        List<String> methodsInClass = List.of("getPrivateProp",
                "getPrivatePropWithJsonIgnore",
                "getPrivatePropWithJsonIgnoreProperties",
                "getPrivateTransientPropWithGetter", "getPropertyGetterOnly",
                "setPropertyGetterOnly", "getPropertyWithDifferentField",
                "setPropertyWithDifferentField", "getRenamedPrivateProp",
                "setPropertySetterOnly");
        List<String> declaredMethods = Stream
                .of(Sample.class.getDeclaredMethods()).map(Method::getName)
                .filter(name -> !name.equals("$jacocoInit")).toList();
        if (declaredMethods.size() != methodsInClass.size()) {
            throw new IllegalStateException(methodsInClass.size()
                    + " methods defined, " + declaredMethods.size() + " found. "
                    + "If you modify methods in the " + Sample.class.getName()
                    + " you need to update this method");
        }
        return declaredMethods.equals(methodsInClass);
    }
}
