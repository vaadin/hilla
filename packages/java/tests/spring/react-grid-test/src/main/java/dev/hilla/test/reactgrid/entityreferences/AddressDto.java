package dev.hilla.test.reactgrid.entityreferences;

public record AddressDto(String street, String city, String country) {
    public static AddressDto fromEntity(Address entity) {
        return new AddressDto(entity.getStreetAddress(), entity.getCity(), entity.getCountry());
    }
}
