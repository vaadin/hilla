package com.vaadin.flow.spring.fusionsecurity.endpoints;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class SessionEndpoint {

    public void invalidateSessionIfPresent() {
        WrappedSession wrappedSession = VaadinRequest.getCurrent()
                .getWrappedSession(false);
        if (wrappedSession != null) {
            wrappedSession.invalidate();
        }
    }

}
