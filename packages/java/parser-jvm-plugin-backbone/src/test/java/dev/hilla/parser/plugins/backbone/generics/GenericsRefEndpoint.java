package dev.hilla.parser.plugins.backbone.generics;

@Endpoint
public class GenericsRefEndpoint<T extends GenericsRefEntity<String>> {
    public T getReference(T ref) {
        return ref;
    }
}
