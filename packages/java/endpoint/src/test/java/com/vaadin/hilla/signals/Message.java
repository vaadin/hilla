package com.vaadin.hilla.signals;

import java.time.LocalDateTime;

public record Message(String text, String author, LocalDateTime timestamp) {
}
