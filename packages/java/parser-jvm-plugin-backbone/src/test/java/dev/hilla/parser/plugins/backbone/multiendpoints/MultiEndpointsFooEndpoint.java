package dev.hilla.parser.plugins.backbone.multiendpoints;

@Endpoint
public class MultiEndpointsFooEndpoint {
    public String getFoo() {
        return "foo";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
