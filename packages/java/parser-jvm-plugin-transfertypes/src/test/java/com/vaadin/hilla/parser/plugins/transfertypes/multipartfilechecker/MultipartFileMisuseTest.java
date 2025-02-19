package com.vaadin.hilla.parser.plugins.transfertypes.multipartfilechecker;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileUsageException;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;

public class MultipartFileMisuseTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ThrowWhenMultipartFileIsUsedInEntity() {
        assertThrows(MultipartFileUsageException.class,
                () -> new Parser()
                        .classPath(Set.of(helper.getTargetDir().toString()))
                        .endpointAnnotations(List.of(Endpoint.class))
                        .addPlugin(new BackbonePlugin())
                        .addPlugin(new MultipartFileCheckerPlugin())
                        .execute(List.of(MultipartFileInEntityEndpoint.class)));
    }

    @Test
    public void should_ThrowWhenMultipartFileIsUsedAsReturnType() {
        assertThrows(MultipartFileUsageException.class, () -> new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new MultipartFileCheckerPlugin())
                .execute(List.of(MultipartFileAsReturnTypeEndpoint.class)));
    }
}
