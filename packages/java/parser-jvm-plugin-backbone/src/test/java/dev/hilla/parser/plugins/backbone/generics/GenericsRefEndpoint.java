package dev.hilla.parser.plugins.backbone.generics;

@Endpoint
public class GenericsRefEndpoint<T extends GenericsBareRefEntity<String>, U extends GenericsExtendedRefEntity<GenericsBareRefEntity<String>>> {
    public T getBareReference(T ref) {
        return ref;
    }

    public U getExtendedReference(U ref) {
        return ref;
    }
}
