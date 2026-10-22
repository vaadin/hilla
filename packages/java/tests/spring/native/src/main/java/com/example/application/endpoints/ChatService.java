/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.application.endpoints;

import jakarta.annotation.security.PermitAll;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.hilla.Endpoint;

@Endpoint
@PermitAll
class ChatService {

    private final AuthenticationContext authContext;

    record Message(String userName, String text, Instant time) {
    }

    ChatService(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    private final Sinks.Many<Message> chatSink = Sinks.many().multicast()
            .directBestEffort();

    private final Flux<Message> chat = chatSink.asFlux();

    public Flux<@NonNull Message> join() {
        return chat;
    }

    public void send(String message) {
        chatSink.tryEmitNext(
                new Message(authContext.getPrincipalName().orElse("Anonymous"),
                        message, Instant.now()));
    }

}
