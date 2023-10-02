package com.example.application.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonRepository
        extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
