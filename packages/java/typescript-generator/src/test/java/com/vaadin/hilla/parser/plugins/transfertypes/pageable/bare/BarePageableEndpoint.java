package com.vaadin.hilla.parser.plugins.transfertypes.pageable.bare;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import org.springframework.data.domain.Sort;

@Endpoint
public class BarePageableEndpoint {
    public Sort getSort() {
        return null;
    }
}
