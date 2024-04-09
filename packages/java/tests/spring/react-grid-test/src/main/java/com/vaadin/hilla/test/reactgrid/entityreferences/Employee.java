package com.vaadin.hilla.test.reactgrid.entityreferences;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.util.List;

import com.vaadin.hilla.test.reactgrid.AbstractEntity;

@Entity
public class Employee extends AbstractEntity {

    private String name;

    @OneToOne
    private Address homeAddress;

    @ManyToOne
    private Department department;

    @ManyToMany
    private List<FoodItem> allergies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address address) {
        this.homeAddress = address;
    }

    public List<FoodItem> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<FoodItem> allergies) {
        this.allergies = allergies;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
