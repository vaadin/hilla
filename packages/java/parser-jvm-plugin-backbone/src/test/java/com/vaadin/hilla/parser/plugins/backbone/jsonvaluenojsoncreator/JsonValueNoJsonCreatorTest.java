package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.JsonValuePlugin.MalformedValueTypeException;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonValueNoJsonCreatorTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ThrowExceptionWhenOnlyJsonValueIsUsed() {
        assertThrows(MalformedValueTypeException.class, () -> {
            new Parser().classLoader(getClass().getClassLoader())
                    .classPath(Set.of(helper.getTargetDir().toString()))
                    .endpointAnnotations(List.of(Endpoint.class))
                    .addPlugin(new BackbonePlugin())
                    .execute(List.of(JsonValueNoJsonCreatorTest.class));
        });
    }
}
