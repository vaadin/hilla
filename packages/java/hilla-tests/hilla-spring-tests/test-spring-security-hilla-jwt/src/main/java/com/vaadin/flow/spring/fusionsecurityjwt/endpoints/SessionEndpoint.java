package com.vaadin.flow.spring.fusionsecurityjwt.endpoints;

import dev.hilla.Endpoint;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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
