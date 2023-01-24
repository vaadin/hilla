package dev.hilla.parser.plugins.nonnull.extended;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.hilla.parser.plugins.nonnull.basic.BasicEndpoint;

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
        @Nonnull
        private List<String> nonnullListOfNullableStrings;
    }
}
