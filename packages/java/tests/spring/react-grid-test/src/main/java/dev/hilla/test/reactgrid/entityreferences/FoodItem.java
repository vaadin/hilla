package dev.hilla.test.reactgrid.entityreferences;

import jakarta.persistence.Entity;

import dev.hilla.test.reactgrid.AbstractEntity;

@Entity
public class FoodItem extends AbstractEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
