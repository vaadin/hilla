package dev.hilla.parser.models;

public interface OwnedModel<Owner> {
    Owner getOwner();
}
