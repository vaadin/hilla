package dev.hilla.parser.plugins.backbone.transients;

@Endpoint
public class TransientEndpoint {
    public TransientModel getTransientModel() {
        return new TransientModel();
    }

    public static class TransientModel {
        public NonTransientEntity nonTransientEntity;
        public String notTransientField;
        public transient TransientEntity transientEntity;
        public transient String transientField;
    }
}
