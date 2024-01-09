package com.vaadin.hilla.devmode.devtools;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.server.frontend.TypeScriptBootstrapModifier;

public class DevToolsDatabaseLoader implements TypeScriptBootstrapModifier {

    @Override
    public void modify(List<String> bootstrapTypeScript,
            boolean productionMode) {
        if (productionMode) {
            return;
        }
        String lines = """
                //@ts-ignore
                if (import.meta.env.DEV) {
                    import("Frontend/generated/jar-resources/dev-tools-database.js");
                }
                """;

        bootstrapTypeScript.addAll(Arrays.asList(lines.split("\n")));
    }

}
