package com.vaadin.hilla.parser.plugins.nonnull.extended;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.hilla.parser.plugins.nonnull.basic.BasicEndpoint;

@Endpoint
public class ExtendedEndpoint extends BasicEndpoint {
    @Nonnull
    public List<Entity> getNonnullListOfNullableElements() {
        return Collections.emptyList();
    }

    public List<Map<String, List<Map<String, @Nonnull String>>>> superComplexType(
            List<Map<String, List<Map<String, @Nonnull String>>>> list) {
        return list;
    }

    public static class Entity {
        private List<String> nonnullListOfNullableStrings;

        @Nonnull
        public List<String> getNonnullListOfNullableStrings() {
            return nonnullListOfNullableStrings;
        }
    }
}
