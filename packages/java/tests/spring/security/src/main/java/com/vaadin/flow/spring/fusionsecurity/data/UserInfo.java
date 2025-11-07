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
package com.vaadin.flow.spring.fusionsecurity.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class UserInfo {

    @Id
    @GeneratedValue
    @JsonIgnore
    private int id;

    private String username;
    @JsonIgnore
    private String encodedPassword;
    private String fullName;
    private String imageUrl;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    public UserInfo() {

    }

    public UserInfo(String username, String encodedPassword, String fullName,
            String imageUrl, String... roles) {
        this.username = username;
        this.encodedPassword = encodedPassword;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.roles = new ArrayList<String>();
        Collections.addAll(this.roles, roles);
    }

    public int getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }
}
