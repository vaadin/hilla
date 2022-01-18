package dev.hilla.generator.endpoints.superclassmethods;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.EndpointExposed;

@EndpointExposed
@AnonymousAllowed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public T update(T entity) {
        return entity;
    }

    public void delete(ID id) {
    }
}
