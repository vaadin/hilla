/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.test.reactgrid.entityreferences;

import jakarta.persistence.Entity;
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
