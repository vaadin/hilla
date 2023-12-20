package com.example.application.endpoints;

import jakarta.annotation.security.PermitAll;

import java.time.Instant;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import com.vaadin.flow.spring.security.AuthenticationContext;

@Endpoint
@PermitAll
class ChatService {

	private final AuthenticationContext authContext;

	record Message(String userName, String text, Instant time) {
	}

	ChatService(AuthenticationContext authContext) {
		this.authContext = authContext;
	}

	private final Sinks.Many<Message> chatSink = Sinks.many().multicast().directBestEffort();

	private final Flux<Message> chat = chatSink.asFlux();

	public Flux<@Nonnull Message> join() {
		return chat;
	}

	public void send(String message) {
		chatSink.tryEmitNext(new Message(authContext.getPrincipalName().orElse("Anonymous"), message, Instant.now()));
	}

}
