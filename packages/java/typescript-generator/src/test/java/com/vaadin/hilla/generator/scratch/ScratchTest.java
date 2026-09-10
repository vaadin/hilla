package com.vaadin.hilla.generator.scratch;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.Nonnull;
import com.vaadin.hilla.generator.fixtures.SignalsEndpoint;
import com.vaadin.hilla.generator.typescript.ClientWriter;
import com.vaadin.hilla.generator.typescript.EndpointWriter;
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

public class ScratchTest {
    @Test
    public void dump() {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(
                Set.of(new AnnotationMatcher(Nonnull.class.getName(), false,
                        10)),
                null));

        var generator = new FullStackGenerator(ScratchTest.class,
                SignalsEndpoint.class).withPlugin(plugin);

        generator.parseModel().forEach(endpoint -> {
            var file = new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                    .write(endpoint);
            System.out.println("##### " + file.path());
            System.out.println(file.content());
        });
        System.out.println("###node "
                + generator.generate().get("SignalsEndpoint.ts"));
    }
}
