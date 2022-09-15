package dev.hilla.parser.plugins.backbone.bare;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Endpoint
public class BareTypeEndpoint {
    public Map getBareMap() {
        return null;
    }

    public List getBareList() {
        return null;
    }

    public Optional getBareOptional() {
        return Optional.empty();
    }
}
