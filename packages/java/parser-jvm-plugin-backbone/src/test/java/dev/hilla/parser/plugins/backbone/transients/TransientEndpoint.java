package dev.hilla.parser.plugins.backbone.transients;

@Endpoint
public class TransientEndpoint {
    public TransientModel getTransientModel() {
        return new TransientModel();
    }

    public static class TransientModel {
        private NonTransientEntity nonTransientEntity;
        private String notTransientField;
        private transient TransientEntity transientEntity;
        private transient String transientField;
    }
}
