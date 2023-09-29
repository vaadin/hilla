package com.example.application.data.entity;

import com.example.application.data.Role;

import java.util.Set;

public class User {

    private String username;

    private Set<Role> roles;

    public User(String username, Role... roles) {
        this.username = username;
        this.roles = Set.of(roles);
    }

    public String getUsername() {
        return username;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
