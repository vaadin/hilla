package com.vaadin.flow.spring.fusionsecurity.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
