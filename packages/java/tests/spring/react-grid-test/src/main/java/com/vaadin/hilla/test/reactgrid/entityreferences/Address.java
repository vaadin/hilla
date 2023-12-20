package com.vaadin.hilla.test.reactgrid.entityreferences;

import jakarta.persistence.Entity;

import com.vaadin.hilla.test.reactgrid.AbstractEntity;

@Entity
public class Address extends AbstractEntity {

    private String streetAddress;
    private String city;
    private String country;

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
