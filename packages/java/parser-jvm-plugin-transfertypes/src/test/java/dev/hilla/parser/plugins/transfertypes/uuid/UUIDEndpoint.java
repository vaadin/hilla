package dev.hilla.parser.plugins.transfertypes.uuid;

import java.util.UUID;

@Endpoint
public class UUIDEndpoint {
    public UUID getUUID() {
        return new UUID(10, 20);
    }
}
