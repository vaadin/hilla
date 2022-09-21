package dev.hilla.parser.plugins.backbone.transients;

@Endpoint
public class TransientEndpoint {
    public TransientModel getTransientModel() {
        return new TransientModel();
    }

    public static class TransientModel {
        private String notTransientField;
        private transient String transientField;
        private transient TransientEntity transientEntity;
        private NonTransientEntity nonTransientEntity;
    }
}
