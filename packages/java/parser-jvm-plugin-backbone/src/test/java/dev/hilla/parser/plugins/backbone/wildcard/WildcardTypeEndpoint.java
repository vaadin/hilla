package dev.hilla.parser.plugins.backbone.wildcard;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Endpoint
public class WildcardTypeEndpoint {
    public Map<String, ?> getDefaultWildcard() {
        return null;
    }

    public List<? extends Map<Object, Object>> getExtendingWildcard() {
        return null;
    }

    public Optional<? super List<Object>> getSuperWildcard() {
        return Optional.empty();
    }
}
