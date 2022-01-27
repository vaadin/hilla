package dev.hilla.parser.plugins.nonnull.extended;

import java.util.List;
import java.util.Map;

import dev.hilla.parser.plugins.nonnull.basic.BasicEndpoint;

@Endpoint
public class ExtendedEndpoint extends BasicEndpoint {
    public List<Map<String, List<Map<String, @Nonnull String>>>> superComplexType(
            List<Map<String, List<Map<String, @Nonnull String>>>> list) {
        return list;
    }
}
